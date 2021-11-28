package org.student.zccchanda.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.student.zccchanda.model.Ticket;
import org.student.zccchanda.service.ITicketService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class TicketController {
    @Autowired
    private ITicketService ticketService;

    @RequestMapping(value = "/tickets", method = RequestMethod.GET)
    public String listTickets(Model model, @RequestParam(value = "page", required = false) String page, @RequestParam(value="b", required = false) String b,HttpServletRequest request){
        List<Ticket> tickets = ticketService.getTickets(b, page);
        model.addAttribute("tickets", tickets);
        model.addAttribute("hasMore", request.getAttribute("hasMore"));
        model.addAttribute("beforeCursor", request.getAttribute("beforeCursor"));
        model.addAttribute("nextCursor", request.getAttribute("nextCursor"));
        model.addAttribute("errorMsg", request.getAttribute("errorMsg"));
        return "list-tickets";
    }

    @RequestMapping(value = "/tickets/{ticketId}", method = RequestMethod.GET)
    public String getTicket(Model model, @PathVariable long ticketId, HttpServletRequest request){
        Ticket ticket = ticketService.getTicket(ticketId);
        if(ticket != null) {
            model.addAttribute("ticket", ticket);
        } else {
            model.addAttribute("ticket", "undefined");
        }
        model.addAttribute("errorMsg", request.getAttribute("errorMsg"));
        return "list-ticket";
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason="Argument should be valid long")
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleTypeMismatchException(MethodArgumentTypeMismatchException exception, HttpServletRequest request){
        String name = exception.getName();
        String type = exception.getRequiredType().getSimpleName();
        Object value = exception.getValue();
        String message = String.format("'%s' should be a valid '%s' and '%s' isn't", name, type, value);
        request.setAttribute("errorMsg", message);
    }
}
