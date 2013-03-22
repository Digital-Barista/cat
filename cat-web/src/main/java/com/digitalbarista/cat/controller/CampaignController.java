package com.digitalbarista.cat.controller;

import com.digitalbarista.cat.business.*;
import com.digitalbarista.cat.ejb.session.CampaignManager;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;

@Controller
public class CampaignController
{
    @Autowired
    private CampaignManager campaignManager;

    @RequestMapping(value = "/data/broadcast")
    public ServiceResponse listBroadcasts(@RequestParam(required = false) Integer offset,
                                       @RequestParam(required = false) Integer limit,
                                       @RequestParam(required = false) String sort,
                                       @RequestParam(required = false) Integer direction)
    {
        ServiceMetadata meta = new ServiceMetadata(limit, offset, sort, direction);
        ServiceResponse ret = campaignManager.getBroadcastCampaigns(null, meta);
        return ret;
    }

    @RequestMapping(value = "/data/broadcast", method = RequestMethod.POST)
    public ServiceResponse broadcastMessage(@RequestBody String body){
        ServiceResponse ret = new ServiceResponse();
        try {
            JSONObject json = new JSONObject(body);
            JAXBContext context = JAXBContext.newInstance(EntryData.class);
            Unmarshaller unmarsh = context.createUnmarshaller();

            Long clientId = new Long(json.get("clientId").toString());
            MessageNode message = new MessageNode();
            message.setMessage(json.get("message").toString());

            Object entries = unmarsh.unmarshal(new StreamSource(new StringReader(json.get("entryPoints").toString())));
            List<EntryData> entryPoints = (List<EntryData>)entries;

            campaignManager.broadcastMessageSearch(clientId, entryPoints, message, null);
            ret.setResult("message sent");
        }
        catch (Exception e){
            ret.addError(new ServiceError(e.getMessage()));
        }
        return ret;
    }
}
