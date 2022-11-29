package uk.gov.dwp.uc.pairtest;

import java.util.Arrays;
import java.util.Objects;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    private static final int MAXIMUM_NUMBER_OF_TICKETS = 20;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests)
            throws InvalidPurchaseException {
        checkAccountID(accountId);
        checkNumberOfTickets(ticketTypeRequests);
        checkTicketTypes(ticketTypeRequests);

        int totalCosts = calcTicketPrice(ticketTypeRequests);
        int totalSeats = calcSeats(ticketTypeRequests);

        ticketPaymentService.makePayment(accountId, totalCosts);
        seatReservationService.reserveSeat(accountId, totalSeats);
    }

    private void checkAccountID(Long accountId) {
        if (Objects.isNull(accountId) || accountId <= 0) {
            throw new InvalidPurchaseException("Account ID is not greater than 0!");
        }
    }

    private void checkNumberOfTickets(TicketTypeRequest[] ticketTypeRequests) {
        if (Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum() >= MAXIMUM_NUMBER_OF_TICKETS) {
            throw new InvalidPurchaseException("Number of tickets is greater than 19!");
        }
    }

    private void checkTicketTypes(TicketTypeRequest[] ticketTypeRequests) {
        boolean adultTicketAvailable = false;
        boolean nonAdultTicketAvailable = false;

        if (Arrays.stream(ticketTypeRequests)
                .anyMatch(ticketTypeRequest -> ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.CHILD
                        || ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.INFANT)) {
            nonAdultTicketAvailable = true;
        }
        if (Arrays.stream(ticketTypeRequests)
                .anyMatch(ticketTypeRequest -> ticketTypeRequest.getTicketType() == TicketTypeRequest.Type.ADULT)) {
            adultTicketAvailable = true;
        }

        if (!adultTicketAvailable && nonAdultTicketAvailable) {
            throw new InvalidPurchaseException("There has to be at least one adult with a child or an infant!");
        }
    }

    private int calcTicketPrice(TicketTypeRequest[] ticketTypeRequests) {
        int totalCosts = 0;
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            totalCosts += calcTicketTypePrice(ticketTypeRequest) * ticketTypeRequest.getNoOfTickets();
        }

        return totalCosts;
    }

    private int calcTicketTypePrice(TicketTypeRequest ticketTypeRequest) {
        int cost = 0;
        switch (ticketTypeRequest.getTicketType()) {
            case INFANT:
                cost = 0;
                break;
            case CHILD:
                cost = 10;
                break;
            case ADULT:
                cost = 20;
                break;
            default:
                throw new IllegalArgumentException("Invalid ticket type!");
        }

        return cost;
    }

    private int calcSeats(TicketTypeRequest[] ticketTypeRequests) {
        int totalSeats = 0;
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            totalSeats += calcSeatAllocation(ticketTypeRequest) * ticketTypeRequest.getNoOfTickets();
        }

        return totalSeats;
    }

    private int calcSeatAllocation(TicketTypeRequest ticketTypeRequest) {
        int seatsAllocated = 0;
        switch (ticketTypeRequest.getTicketType()) {
            case INFANT:
                seatsAllocated = 0;
                break;
            case CHILD:
                seatsAllocated = 1;
                break;
            case ADULT:
                seatsAllocated = 1;
                break;
            default:
                throw new IllegalArgumentException("Invalid seat allocation!");
        }

        return seatsAllocated;
    }
}
