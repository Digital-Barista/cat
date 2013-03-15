package com.digitalbarista.cat.controller;

import com.digitalbarista.cat.business.Client;
import com.digitalbarista.cat.business.ServiceResponse;
import com.digitalbarista.cat.business.ServiceResponseMetadata;
import com.digitalbarista.cat.ejb.session.ClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * User: Kris
 * Date: 3/15/13
 * Time: 12:53 PM
 */
@Controller
public class ClientController
{
    @Autowired
    private ClientManager clientManager;

    @RequestMapping(value = "/data/client")
    public ServiceResponse listClients()
    {
        ServiceResponse ret = new ServiceResponse();
        List<Client> clients = clientManager.getVisibleClients();
        ret.setResult(clients);

        ServiceResponseMetadata meta = new ServiceResponseMetadata();
        meta.setTotal(clients.size());
        ret.setMetadata(meta);

        return ret;
    }
}
