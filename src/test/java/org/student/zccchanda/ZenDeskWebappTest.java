package org.student.zccchanda;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ZenDeskWebappTest extends ZendeskTicketingApplicationTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeAll
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testTickets() throws Exception {
        MvcResult result = mockMvc.perform(get("/tickets")).andReturn();
        assert(result.getResponse().getStatus()==200);
        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assert(content.indexOf("<a href=\"/tickets/11\">view</a>") !=-1);
    }
    @Test
    public void testFailedTicket() throws Exception {
        MvcResult result = mockMvc.perform(get("/tickets/1000")).andReturn();
        assert(result.getResponse().getStatus()==200);
        assert(result.getResponse().getContentAsString().indexOf("RecordNotFound") !=-1);

    }
    @Test
    public void testSuccessTicket() throws Exception {
        MvcResult result = mockMvc.perform(get("/tickets/15")).andReturn();
        assert(result.getResponse().getStatus()==200);
        assert(result.getResponse().getContentAsString().indexOf("RecordNotFound") ==-1);

    }
    @Test
    public void testBadURL() throws Exception {
        MvcResult result = mockMvc.perform(get("/wrongticke")).andReturn();
        assert(result.getResponse().getStatus()==404);

    }
}
