package uk.gov.dwp.uc.pairtest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @Spy
    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    public void accountIDValid() {
        TicketTypeRequest ticket = new TicketTypeRequest(Type.ADULT, 1);
        ticketService.purchaseTickets(1L, ticket);

        verify(ticketService).purchaseTickets(1L, ticket);
    }

    @Test
    public void accountIDInvalidPartOne() {
        exception.expect(InvalidPurchaseException.class);
        exception.expectMessage("Account ID is not greater than 0!");
        ticketService.purchaseTickets(-1L, new TicketTypeRequest(Type.ADULT, 1));
    }

    @Test
    public void accountIDInvalidPartTwo() {
        exception.expect(InvalidPurchaseException.class);
        exception.expectMessage("Account ID is not greater than 0!");
        ticketService.purchaseTickets(0L, new TicketTypeRequest(Type.ADULT, 1));
    }

    @Test
    public void maxNumberOfTicketsExceededPartOne() {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.ADULT, 20);
        TicketTypeRequest[] ticketTypeRequests = { ticket1 };

        exception.expect(InvalidPurchaseException.class);
        exception.expectMessage("Number of tickets is greater than 19!");

        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    public void maxNumberOfTicketsExceededPartTwo() {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest ticket2 = new TicketTypeRequest(Type.CHILD, 25);
        TicketTypeRequest[] ticketTypeRequests = { ticket1, ticket2 };

        exception.expect(InvalidPurchaseException.class);
        exception.expectMessage("Number of tickets is greater than 19!");

        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    public void atLeastOneAdultWithAChildOrInfantPartOne() {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.INFANT, 1);
        TicketTypeRequest ticket2 = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest[] ticketTypeRequests = { ticket1, ticket2 };

        exception.expect(InvalidPurchaseException.class);
        exception.expectMessage("There has to be at least one adult with a child or an infant!");

        ticketService.purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    public void atLeastOneAdultWithAChildOrInfantPartTwo() {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.INFANT, 1);
        TicketTypeRequest ticket2 = new TicketTypeRequest(Type.CHILD, 1);
        TicketTypeRequest ticket3 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest[] ticketTypeRequests = { ticket1, ticket2, ticket3 };

        ticketService.purchaseTickets(1L, ticketTypeRequests);

        verify(ticketService, times(1)).purchaseTickets(1L, ticketTypeRequests);
    }

    @Test
    public void makePaymentPartOne() throws Exception {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest ticket2 = new TicketTypeRequest(Type.CHILD, 4);
        TicketTypeRequest[] ticketTypeRequests = { ticket1, ticket2 };

        Long accountId = 1L;

        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        int expectedCost = 80;

        verify(ticketPaymentService).makePayment(accountId, expectedCost);
    }

    @Test
    public void makePaymentPartTwo() throws Exception {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest ticket2 = new TicketTypeRequest(Type.CHILD, 4);
        TicketTypeRequest ticket3 = new TicketTypeRequest(Type.INFANT, 4);

        TicketTypeRequest[] ticketTypeRequests = { ticket1, ticket2, ticket3 };

        Long accountId = 1L;

        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        int expectedCost = 80;

        verify(ticketPaymentService).makePayment(accountId, expectedCost);
    }

    @Test
    public void reserveSeatsPartOne() throws Exception {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest ticket2 = new TicketTypeRequest(Type.CHILD, 4);
        TicketTypeRequest[] ticketTypeRequests = { ticket1, ticket2 };

        Long accountId = 1L;

        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        int expectedSeats = 6;

        verify(seatReservationService).reserveSeat(accountId, expectedSeats);
    }

    @Test
    public void reserveSeatsPartTwo() throws Exception {
        TicketTypeRequest ticket1 = new TicketTypeRequest(Type.ADULT, 2);
        TicketTypeRequest ticket2 = new TicketTypeRequest(Type.CHILD, 4);
        TicketTypeRequest ticket3 = new TicketTypeRequest(Type.INFANT, 4);

        TicketTypeRequest[] ticketTypeRequests = { ticket1, ticket2, ticket3 };

        Long accountId = 1L;

        ticketService.purchaseTickets(accountId, ticketTypeRequests);

        int expectedSeats = 6;

        verify(seatReservationService).reserveSeat(accountId, expectedSeats);
    }
}
