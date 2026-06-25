package com.example.hotelbooking.Services.AI;

import com.example.hotelbooking.ExceptionHandler.CustomerNotFoundException;
import com.example.hotelbooking.ExceptionHandler.HotelNotFoundException;
import com.example.hotelbooking.Models.*;
import com.example.hotelbooking.Respositories.*;
import com.example.hotelbooking.Services.Gemini.GeminiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.example.hotelbooking.DTOs.AI.ChatRequest;

import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

     private final GeminiService geminiService;
     private final HotelRepository hotelRepository;
     private final BookingRepository bookingRepository;
     private final ReviewRepository reviewRepository;
     private final RoomRepository roomRepository;
     private final CustomerProfileRepository customerProfileRepository;
     private final HotelManagerRepository hotelManagerRepository;

    public String customerChat(String userMessage, String context, String contextId) {

        // Step 1 — extract structured parameters from user message
        String extractionPrompt =
                "Extract parameters from this message: \"" + userMessage + "\"\n" +
                        "Reply ONLY as JSON with these exact fields (use null if not mentioned):\n" +
                        "{\"checkIn\": \"YYYY-MM-DD\", \"checkOut\": \"YYYY-MM-DD\", \"budget\": 0, \"occupancy\": 0}\n" +
                        "No explanation, no markdown, just the raw JSON object.";

        String extractedJson = geminiService.generateResponse(extractionPrompt);
        LocalDate checkIn = null;
        LocalDate checkOut = null;
        Double budget = null;
        Integer occupancy = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode params = mapper.readTree(extractedJson);
            if (params.hasNonNull("checkIn"))   checkIn   = LocalDate.parse(params.get("checkIn").asText());
            if (params.hasNonNull("checkOut"))  checkOut  = LocalDate.parse(params.get("checkOut").asText());
            if (params.hasNonNull("budget") && params.get("budget").asDouble() > 0)
                budget    = params.get("budget").asDouble();
            if (params.hasNonNull("occupancy") && params.get("occupancy").asInt() > 0)
                occupancy = params.get("occupancy").asInt();
        } catch (Exception e) {
            // if extraction fails, proceed with nulls — queries will just skip filtering
        }

        // Step 2 — classify intent
        String classificationPrompt =
                "You are a hotel assistant. A customer asked: \"" + userMessage + "\"\n" +
                        "Which of these data types are needed to answer? Reply with ONLY a comma-separated list.\n" +
                        "Options: FETCH_HOTELS, FETCH_ROOMS, FETCH_BOOKINGS, FETCH_REVIEWS\n" +
                        "Example response: FETCH_HOTELS,FETCH_REVIEWS\n" +
                        "Rules:\n" +
                        "- If the question mentions price, budget, or room type, always include FETCH_ROOMS\n" +
                        "- If the question mentions hotels, always include FETCH_HOTELS\n" +
                        "- If unsure, include FETCH_HOTELS as default.";

        String intentResponse = geminiService.generateResponse(classificationPrompt);
        Set<String> intents = parseIntents(intentResponse);

        // Step 3 — build prompt with fetched data
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful hotel booking assistant.\n");
        prompt.append("Answer based only on the data provided below. Be concise.\n\n");

        // resolve booked room IDs once — reused across all blocks that need it
        final LocalDate finalCheckIn = checkIn;
        final LocalDate finalCheckOut = checkOut;
        final Double finalBudget = budget;
        final Integer finalOccupancy = occupancy;

        Set<Long> bookedRoomIds = (finalCheckIn != null && finalCheckOut != null)
                ? bookingRepository.findBookedRoomIdsBetween(finalCheckIn, finalCheckOut)
                : bookingRepository.findCurrentlyBookedRoomIds();

        if (intents.contains("FETCH_HOTELS") && intents.contains("FETCH_ROOMS")) {
            List<Hotel> hotels = hotelRepository.findAll();
            List<Room> allRooms = roomRepository.findAll();

            Map<Long, List<Room>> roomsByHotelId = allRooms.stream()
                    .collect(Collectors.groupingBy(r -> r.getHotel().getId()));

            prompt.append("Hotels with their available rooms:\n");
            hotels.forEach(h -> {
                prompt.append("\nHotel: " + h.getName() +
                        " | Rating: " + h.getStarRating() + "/5\n");

                List<Room> rooms = roomsByHotelId.getOrDefault(h.getId(), Collections.emptyList());

                // apply occupancy and budget filters on Java side before sending to AI
                List<Room> filteredRooms = rooms.stream()
                        .filter(r -> finalOccupancy == null || r.getMaxOccupancy() >= finalOccupancy)
                        .filter(r -> finalBudget == null || r.getPricePerNight().doubleValue() <= finalBudget)
                        .collect(Collectors.toList());

                if (filteredRooms.isEmpty()) {
                    prompt.append("  No rooms match the given filters.\n");
                } else {
                    Map<String, List<Room>> roomsByType = filteredRooms.stream()
                            .collect(Collectors.groupingBy(r -> r.getRoomType().toString()));

                    roomsByType.forEach((type, roomsOfType) -> {
                        long availableCount = roomsOfType.stream()
                                .filter(r -> !bookedRoomIds.contains(r.getId()))
                                .count();
                        long totalCount = roomsOfType.size();
                        Room sample = roomsOfType.get(0);

                        prompt.append(
                                "  - " + type +
                                        " | ₹" + sample.getPricePerNight() + "/night" +
                                        " | Max occupancy: " + sample.getMaxOccupancy() +
                                        " | Available: " + availableCount + "/" + totalCount + "\n"
                        );
                    });
                }
            });

        } else if (intents.contains("FETCH_HOTELS")) {
            List<Hotel> hotels = hotelRepository.findAll();
            prompt.append("Available hotels:\n");
            hotels.forEach(h -> prompt.append(
                    "- " + h.getName() + " | Rating: " + h.getStarRating() + "/5\n"
            ));

        } else if (intents.contains("FETCH_ROOMS")) {
            List<Room> rooms = contextId != null && !contextId.isEmpty()
                    ? roomRepository.findAllByHotel_Id(
                    hotelRepository.findByPublicId(contextId)
                            .orElseThrow(() -> new HotelNotFoundException("Hotel Not Found")).getId())
                    : roomRepository.findAll();

            // apply filters
            List<Room> filteredRooms = rooms.stream()
                    .filter(r -> finalOccupancy == null || r.getMaxOccupancy() >= finalOccupancy)
                    .filter(r -> finalBudget == null || r.getPricePerNight().doubleValue() <= finalBudget)
                    .collect(Collectors.toList());

            prompt.append("Available rooms:\n");

            // group by type and show availability count
            Map<String, List<Room>> roomsByType = filteredRooms.stream()
                    .collect(Collectors.groupingBy(r -> r.getRoomType().toString()));

            roomsByType.forEach((type, roomsOfType) -> {
                long availableCount = roomsOfType.stream()
                        .filter(r -> !bookedRoomIds.contains(r.getId()))
                        .count();
                long totalCount = roomsOfType.size();
                Room sample = roomsOfType.get(0);

                prompt.append(
                        "- " + type +
                                " | ₹" + sample.getPricePerNight() + "/night" +
                                " | Max occupancy: " + sample.getMaxOccupancy() +
                                " | Available: " + availableCount + "/" + totalCount + "\n"
                );
            });
        }

        if (intents.contains("FETCH_BOOKINGS")) {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            CustomerProfile customer = customerProfileRepository
                    .findByUserId(userDetails.getUser().getId())
                    .orElseThrow(() -> new CustomerNotFoundException("Customer profile not found"));

            List<Booking> bookings = bookingRepository.findAllByCustomerId(customer.getId());

            Set<Long> hotelIds = new HashSet<>();
            Set<Long> roomIds = new HashSet<>();
            bookings.forEach(b -> {
                hotelIds.add(b.getHotel().getId());
                roomIds.add(b.getRoom().getId());
            });

            Map<Long, Hotel> hotelMap = hotelRepository.findAllById(hotelIds)
                    .stream()
                    .collect(Collectors.toMap(Hotel::getId, h -> h));

            Map<Long, Room> roomMap = roomRepository.findAllById(roomIds)
                    .stream()
                    .collect(Collectors.toMap(Room::getId, r -> r));

            LocalDate today = LocalDate.now();

            List<Booking> upcoming = bookings.stream()
                    .filter(b -> b.getCheckOut().isAfter(today))
                    .collect(Collectors.toList());

            List<Booking> past = bookings.stream()
                    .filter(b -> !b.getCheckOut().isAfter(today))
                    .collect(Collectors.toList());

            prompt.append("\nToday's date is: ").append(today).append("\n");

            prompt.append("\nUPCOMING BOOKINGS:\n");
            if (upcoming.isEmpty()) {
                prompt.append("None\n");
            } else {
                upcoming.forEach(b -> {
                    Hotel hotel = hotelMap.get(b.getHotel().getId());
                    Room room = roomMap.get(b.getRoom().getId());
                    prompt.append(
                            "- Hotel: " + hotel.getName() +
                                    " | Room: " + room.getRoomType() +
                                    " | Check-In: " + b.getCheckIn() +
                                    " | Check-Out: " + b.getCheckOut() +
                                    " | Amount: ₹" + b.getTotalAmount() +
                                    " | Status: " + b.getBookingStatus() + "\n"
                    );
                });
            }

            prompt.append("\nPAST BOOKINGS:\n");
            if (past.isEmpty()) {
                prompt.append("None\n");
            } else {
                past.stream().limit(5).forEach(b -> {
                    Hotel hotel = hotelMap.get(b.getHotel().getId());
                    Room room = roomMap.get(b.getRoom().getId());
                    prompt.append(
                            "- Hotel: " + hotel.getName() +
                                    " | Room: " + room.getRoomType() +
                                    " | Check-In: " + b.getCheckIn() +
                                    " | Check-Out: " + b.getCheckOut() +
                                    " | Amount: ₹" + b.getTotalAmount() +
                                    " | Status: " + b.getBookingStatus() + "\n"
                    );
                });
            }
        }

        if (intents.contains("FETCH_REVIEWS")) {
            List<Review> reviews = contextId != null && !contextId.isEmpty()
                    ? reviewRepository.findAllByHotelId(
                    hotelRepository.findByPublicId(contextId)
                            .orElseThrow(() -> new HotelNotFoundException("Hotel not found")).getId())
                    : reviewRepository.findAll();

            prompt.append("\nReviews:\n");
            reviews.stream().limit(10).forEach(r -> prompt.append(
                    "- Hotel: " + r.getHotel().getName() +
                            " | Rating: " + r.getRating() + "/5 | " + r.getComment() + "\n"
            ));
        }

        prompt.append("\nThe customer is currently on the " + context + ".\n");
        prompt.append("Use this as a soft hint to tailor your response to where they are.\n");
        prompt.append("\nCustomer question: ").append(userMessage);
        prompt.append("\nIf the answer isn't in the data above, say you don't have that information.");

        return geminiService.generateResponse(prompt.toString());
    }

    private Set<String> parseIntents(String intentResponse) {
        Set<String> intents = new HashSet<>();
        String[] parts = intentResponse.split(",");
        for (String part : parts) {
            intents.add(part.trim().toUpperCase());
        }
        return intents;
    }

    public String getManagerSummary(ChatRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        User manager = userDetails.getUser();

        List<HotelManager> assignedHotels = hotelManagerRepository.findByUserId(manager.getId());
        if (assignedHotels.isEmpty()) {
            throw new AccessDeniedException("No hotels assigned to you.");
        }

        Hotel hotel = null;
        if (request != null && request.getContextId() != null && !request.getContextId().isEmpty()) {
            hotel = hotelRepository.findByPublicId(request.getContextId())
                    .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));
            
            final Long targetHotelId = hotel.getId();
            boolean isAssigned = assignedHotels.stream()
                    .anyMatch(hm -> hm.getHotel().getId().equals(targetHotelId));
            if (!isAssigned) {
                throw new AccessDeniedException("You are not assigned to manage this hotel.");
            }
        } else {
            hotel = assignedHotels.get(0).getHotel();
        }

        // Fetch Bookings and Reviews
        List<Booking> bookings = bookingRepository.findAllByHotelId(hotel.getId());
        List<Review> reviews = reviewRepository.findAllByHotelId(hotel.getId());

        // Calculate stats
        LocalDate today = LocalDate.now();
        LocalDate startOfThisMonth = today.withDayOfMonth(1);
        LocalDate endOfThisMonth = startOfThisMonth.plusMonths(1).minusDays(1);
        LocalDate startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfThisMonth.minusDays(1);

        List<Booking> bookingsThisMonth = bookings.stream()
                .filter(b -> b.getCheckIn() != null && !b.getCheckIn().isBefore(startOfThisMonth) && !b.getCheckIn().isAfter(endOfThisMonth))
                .collect(Collectors.toList());

        List<Booking> bookingsLastMonth = bookings.stream()
                .filter(b -> b.getCheckIn() != null && !b.getCheckIn().isBefore(startOfLastMonth) && !b.getCheckIn().isAfter(endOfLastMonth))
                .collect(Collectors.toList());

        long totalBookingsThisMonth = bookingsThisMonth.size();
        long totalBookingsLastMonth = bookingsLastMonth.size();

        long confirmedBookingsThisMonth = bookingsThisMonth.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) || "COMPLETED".equalsIgnoreCase(b.getBookingStatus()))
                .count();

        long confirmedBookingsLastMonth = bookingsLastMonth.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) || "COMPLETED".equalsIgnoreCase(b.getBookingStatus()))
                .count();

        BigDecimal revenueThisMonth = bookingsThisMonth.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) || "COMPLETED".equalsIgnoreCase(b.getBookingStatus()))
                .map(Booking::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long cancellationsThisMonth = bookingsThisMonth.stream()
                .filter(b -> "CANCELLED".equalsIgnoreCase(b.getBookingStatus()))
                .count();

        long cancellationsLastMonth = bookingsLastMonth.stream()
                .filter(b -> "CANCELLED".equalsIgnoreCase(b.getBookingStatus()))
                .count();

        // Most booked room type this month
        Map<String, Long> roomTypeCounts = bookingsThisMonth.stream()
                .filter(b -> b.getRoom() != null && b.getRoom().getRoomType() != null)
                .collect(Collectors.groupingBy(b -> b.getRoom().getRoomType().toString(), Collectors.counting()));

        String mostBookedRoomType = roomTypeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Average rating
        double avgRating = reviews.stream()
                .map(Review::getRating)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        // Recent reviews
        List<String> recentReviewComments = reviews.stream()
                .sorted(Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(r -> "- Rating: " + r.getRating() + "/5 | Comment: " + r.getComment())
                .collect(Collectors.toList());

        StringBuilder recentReviewsBuilder = new StringBuilder();
        if (recentReviewComments.isEmpty()) {
            recentReviewsBuilder.append("No reviews available yet.");
        } else {
            recentReviewComments.forEach(c -> recentReviewsBuilder.append(c).append("\n"));
        }

        // Build prompt
        StringBuilder prompt = new StringBuilder();

        // 1. Role
        prompt.append("You are a professional hotel performance analyst.\n");
        prompt.append("Answer based ONLY on the hotel data provided below. Do not use any outside knowledge.\n\n");

        // 2. Data
        prompt.append("Hotel: ").append(hotel.getName()).append("\n");
        prompt.append("Performance data for this month (starting ").append(startOfThisMonth).append("):\n");
        prompt.append("- Total Bookings: ").append(totalBookingsThisMonth).append(" this month vs ").append(totalBookingsLastMonth).append(" last month\n");
        prompt.append("- Confirmed/Completed Bookings: ").append(confirmedBookingsThisMonth).append(" this month vs ").append(confirmedBookingsLastMonth).append(" last month\n");
        prompt.append("- Revenue: ₹").append(revenueThisMonth).append("\n");
        prompt.append("- Most Booked Room Type: ").append(mostBookedRoomType).append("\n");
        prompt.append("- Cancellations: ").append(cancellationsThisMonth).append(" this month vs ").append(cancellationsLastMonth).append(" last month\n");
        prompt.append("- Average Customer Rating: ").append(String.format("%.2f", avgRating)).append("/5 (from ").append(reviews.size()).append(" reviews)\n\n");
        prompt.append("Most Recent Reviews:\n").append(recentReviewsBuilder).append("\n");

        // 3. Only ONE of these runs
        if (request != null && request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            prompt.append("The manager asked: \"").append(request.getMessage()).append("\"\n");
            prompt.append("Answer this specific question using only the hotel data above. Do not generate a full summary unless explicitly asked.\n");
        } else {
            prompt.append("Construct a professional performance summary. Identify review themes, booking trends, cancellation concerns, strengths, and actionable recommendations.\n");
        }

        return geminiService.generateResponse(prompt.toString());
    }

    public String getOwnerReport(ChatRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        User owner = userDetails.getUser();

        List<Hotel> ownerHotels = hotelRepository.findAllByOwnerId(owner.getId());
        if (ownerHotels.isEmpty()) {
            return "You do not own any hotels currently, so there is no performance report to display.";
        }

        LocalDate today = LocalDate.now();
        LocalDate startOfThisMonth = today.withDayOfMonth(1);
        LocalDate endOfThisMonth = startOfThisMonth.plusMonths(1).minusDays(1);
        LocalDate startOfLastMonth = startOfThisMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfThisMonth.minusDays(1);

        StringBuilder hotelDataBuilder = new StringBuilder();

        for (Hotel hotel : ownerHotels) {
            List<Booking> bookings = bookingRepository.findAllByHotelId(hotel.getId());
            List<Review> reviews = reviewRepository.findAllByHotelId(hotel.getId());

            List<Booking> bookingsThisMonth = bookings.stream()
                    .filter(b -> b.getCheckIn() != null && !b.getCheckIn().isBefore(startOfThisMonth) && !b.getCheckIn().isAfter(endOfThisMonth))
                    .collect(Collectors.toList());

            List<Booking> bookingsLastMonth = bookings.stream()
                    .filter(b -> b.getCheckIn() != null && !b.getCheckIn().isBefore(startOfLastMonth) && !b.getCheckIn().isAfter(endOfLastMonth))
                    .collect(Collectors.toList());

            long totalBookingsThisMonth = bookingsThisMonth.size();
            long totalBookingsLastMonth = bookingsLastMonth.size();

            long confirmedBookingsThisMonth = bookingsThisMonth.stream()
                    .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) || "COMPLETED".equalsIgnoreCase(b.getBookingStatus()))
                    .count();

            BigDecimal revenueThisMonth = bookingsThisMonth.stream()
                    .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getBookingStatus()) || "COMPLETED".equalsIgnoreCase(b.getBookingStatus()))
                    .map(Booking::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long cancellationsThisMonth = bookingsThisMonth.stream()
                    .filter(b -> "CANCELLED".equalsIgnoreCase(b.getBookingStatus()))
                    .count();

            double avgRating = reviews.stream()
                    .map(Review::getRating)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(0.0);

            hotelDataBuilder.append("Hotel: ").append(hotel.getName()).append(" (ID: ").append(hotel.getPublicId()).append(")\n");
            hotelDataBuilder.append("  - Bookings: ").append(totalBookingsThisMonth).append(" this month (vs ").append(totalBookingsLastMonth).append(" last month)\n");
            hotelDataBuilder.append("  - Confirmed bookings: ").append(confirmedBookingsThisMonth).append(" this month\n");
            hotelDataBuilder.append("  - Revenue: ₹").append(revenueThisMonth).append(" this month\n");
            hotelDataBuilder.append("  - Cancellations: ").append(cancellationsThisMonth).append(" this month\n");
            hotelDataBuilder.append("  - Average rating: ").append(String.format("%.2f", avgRating)).append("/5\n\n");
        }

        // Build prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional hospitality business consultant.\n");
        prompt.append("Analyze and compare the performance across all hotels owned by: ").append(owner.getUsername()).append("\n\n");
        prompt.append("Here is the performance data for each hotel this month:\n\n");
        prompt.append(hotelDataBuilder.toString());
        prompt.append("Please write a comprehensive portfolio performance report. In your report, address the following:\n");
        prompt.append("1. Compare the performance between hotels (which hotel is performing best overall and why, and which hotel is underperforming).\n");
        prompt.append("2. Highlight overall combined revenue, and identify the main contributors to that revenue.\n");
        prompt.append("3. Compare cancellations across hotels (which hotel has the most cancellations and what might be the cause/impact).\n");
        prompt.append("4. Contrast customer satisfaction (average ratings).\n");
        prompt.append("5. Provide strategic suggestions for improvement tailored to specific hotels to optimize performance across the portfolio.\n\n");
        prompt.append("Ensure the report is professional, structured with clear headers, and written for an executive audience. Do not include raw JSON or metadata.");

        return geminiService.generateResponse(prompt.toString());
    }
}

