package org.student.zccchanda.service.impl;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.student.zccchanda.common.HttpConfiguration;
import org.student.zccchanda.common.HttpUtils;
import org.student.zccchanda.model.Ticket;
import org.student.zccchanda.service.ITicketService;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class TicketService implements ITicketService {
    private static final String AUTH_TOKEN = "app.zendesk.token";
    private static final String GET = "GET";
    private static final String TICKETS_ENDPOINT = "app.zendesk.tickets.endpoint";
    private static final String TICKET_ENDPOINT = "app.zendesk.ticket.endpoint";
    private static final String TICKET_ENDPOINT_TIMEOUT = "app.zendesk.ticket.endpoint.timeout";

    @Autowired
    private Environment environment;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public List<Ticket> getTickets(String backLink, String cursor) {
        List<Ticket> tickets = new LinkedList<>();
        StringBuilder urlBuilder = new StringBuilder(environment.getProperty(TICKETS_ENDPOINT));

        if(backLink != null && "1".equals(backLink)){
            urlBuilder.append("&page[before]=").append(cursor);
        } else if (backLink != null && "0".equals(backLink)){
            urlBuilder.append("&page[after]=").append(cursor);
        }

        HttpConfiguration httpConfiguration = getHttpConfig(urlBuilder.toString());
        String response = httpUtils.processHttpRequest(httpConfiguration);
        if(response != null){
            parseResponse(response, tickets);
        } else {
            //some error occurred during API call
            httpServletRequest.setAttribute("errorMsg", "We are unable to process your request at this time. Please try after some time.");
        }
        return tickets;

    }

    @Override
    public Ticket getTicket(long ticketId) {
        List<Ticket> tickets = new LinkedList<>();
        HttpConfiguration httpConfiguration = getHttpConfig(environment.getProperty(TICKET_ENDPOINT));
        StringBuilder ticketIdBuilder = new StringBuilder(httpConfiguration.getServiceURL()).append(ticketId).append(".json");
        httpConfiguration.setServiceURL(ticketIdBuilder.toString());
        String response = httpUtils.processHttpRequest(httpConfiguration);
        if(response != null){
            parseResponse(response, tickets);
            return tickets.size() > 0? tickets.get(0) : null;
        } else {
            //some error occurred during API call
            httpServletRequest.setAttribute("errorMsg", "We are unable to process your request at this time. Please try after some time.");
            return null;
        }
    }


    private HttpConfiguration getHttpConfig(String url) {
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setMethod(GET);
        httpConfiguration.setServiceURL(url);
        httpConfiguration.getHeaderMap().put("Authorization", getAuthorizationHeader());
        try {
            httpConfiguration.setTimeout(Integer.parseInt(environment.getProperty(TICKET_ENDPOINT_TIMEOUT)));
        } catch (NumberFormatException nfe){
            httpConfiguration.setTimeout(5000);
        }
        return httpConfiguration;
    }


    private boolean parseResponse(String response, List<Ticket> tickets) {
        String prevCursor = null;
        String nextCursor = null;
        boolean hasMore = false;
        try {
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            try {

            } catch (Exception e){
                //no links found, continue;
            }
            for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()){
                if("tickets".equalsIgnoreCase(entry.getKey())){
                    JsonArray ticketsArray = entry.getValue().getAsJsonArray();
                    for (int i = 0 ; i < ticketsArray.size() ; i++){
                        JsonObject ticketJson = ticketsArray.get(i).getAsJsonObject();
                        Ticket ticket = getTicketFromJson(ticketJson, prevCursor, nextCursor);
                        tickets.add(ticket);
                    }
                } else if ("ticket".equalsIgnoreCase(entry.getKey())){
                    JsonObject ticketJson = entry.getValue().getAsJsonObject();
                    Ticket ticket = getTicketFromJson(ticketJson, prevCursor, nextCursor);
                    tickets.add(ticket);
                } else if (jsonObject.get("meta") != null){
                    JsonObject metaJsonObject = jsonObject.getAsJsonObject("meta");
                    prevCursor = metaJsonObject.get("before_cursor").getAsString();
                    nextCursor  = metaJsonObject.get("after_cursor").getAsString();
                    hasMore = metaJsonObject.get("has_more").getAsBoolean();
                    httpServletRequest.setAttribute("hasMore", hasMore);
                    httpServletRequest.setAttribute("beforeCursor", prevCursor);
                    httpServletRequest.setAttribute("nextCursor", nextCursor);
                } else if(jsonObject.get("error") != null){
                    httpServletRequest.setAttribute("errorMsg", jsonObject.get("error").getAsString());
                    return false;
                }
            }
//            jsonObject.entrySet().stream().forEach(entry -> {
//            });
            return true;
        } catch (JsonSyntaxException jsonSyntaxException){
            return false;
        }
    }

    private Ticket getTicketFromJson(JsonObject ticketJson, String previousLink, String nextLink) {
        Ticket ticket = new Ticket();
        ticket.setPreviousLink(previousLink);
        ticket.setNextLink(nextLink);
        ticket.setTicketId(ticketJson.get("id").getAsInt());
        ticket.setDescription(getString(ticketJson.get("description")));
        ticket.setPriority(getString(ticketJson.get("priority")));
        ticket.setStatus(getString(ticketJson.get("status")));
        ticket.setSubject(getString(ticketJson.get("subject")));
        ticket.setUrl("/tickets/" + ticket.getTicketId());
        return ticket;
    }

    private String getAuthorizationHeader(){
        return "Bearer " + environment.getProperty(AUTH_TOKEN);
    }

    private String getString(JsonElement jsonElement){
        try {
            return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
        } catch (Exception e){
            return "";
        }
    }
}
