package org.student.zccchanda.service;

import org.student.zccchanda.model.Ticket;

import java.util.List;

public interface ITicketService {
    List<Ticket> getTickets(String back, String cursor);
    Ticket getTicket(long ticketId);
}
