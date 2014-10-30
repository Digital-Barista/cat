package com.digitalbarista.cat.ejb.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.digitalbarista.cat.business.*;
import com.digitalbarista.cat.util.CriteriaUtil;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.digitalbarista.cat.audit.AuditEvent;
import com.digitalbarista.cat.audit.AuditType;
import com.digitalbarista.cat.data.AddInMessageDO;
import com.digitalbarista.cat.data.AddInMessageType;
import com.digitalbarista.cat.data.CampaignEntryPointDO;
import com.digitalbarista.cat.data.ClientDO;
import com.digitalbarista.cat.data.ClientInfoDO;
import com.digitalbarista.cat.data.EntryPointDO;
import com.digitalbarista.cat.data.EntryPointType;
import com.digitalbarista.cat.data.KeywordDO;
import com.digitalbarista.cat.data.KeywordLimitDO;
import com.digitalbarista.cat.data.ReservedKeywordDO;
import com.digitalbarista.cat.exception.FlexException;
import com.digitalbarista.cat.twitter.mbean.TwitterPollCoordinator;
import com.digitalbarista.cat.util.SecurityUtil;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ClientManager")
@Lazy
@Transactional(propagation = Propagation.REQUIRED)
public class ClientManager {

    public static final Integer DEFAULT_MAX_KEYWORDS = 5;

    @Autowired
    UserManager userManager;

    @Autowired
    TwitterPollCoordinator twitterCoordinator;

    @Autowired
    SecurityUtil securityUtil;

    @Autowired
    SessionFactory sf;

    public List<Client> getClients()
    {
      return (List<Client>)getClients(null).getResult();
    }
    
    public ServiceResponse getClients(ServiceMetadata metadata) {
        ServiceMetadata meta = metadata != null ? metadata : new ServiceMetadata();
        List<Client> clients = new ArrayList<Client>();
        Criteria crit = sf.getCurrentSession().createCriteria(ClientDO.class);
        CriteriaUtil.applyPagingInfo(crit, meta);

        if (!securityUtil.isAdmin()) {
            crit.add(Restrictions.in("id", securityUtil.extractClientIds(sf.getCurrentSession())));
        }

        if (securityUtil.isAdmin() || securityUtil.extractClientIds(sf.getCurrentSession()).size() > 0) {
            meta.setTotal(CriteriaUtil.getTotalResultCount(crit));
            for (ClientDO client : (List<ClientDO>) crit.list()) {
                Client c = new Client();
                c.copyFrom(client);
                clients.add(c);
            }
            for (Client c : clients) {
                fillInEntryPointKeywordData(c);
            }
        }

        ServiceResponse ret = new ServiceResponse();
        ret.setResult(clients);
        ret.setMetadata(meta);
        return ret;
    }

    public List<Client> getVisibleClients()
    {
      return getVisibleClients(null);
    }
    
    public List<Client> getVisibleClients(ServiceMetadata metadata) {
        ServiceMetadata meta = metadata != null ? metadata : new ServiceMetadata();
        List<Client> ret = new ArrayList<Client>();
        Criteria crit = sf.getCurrentSession().createCriteria(ClientDO.class);

        if (!securityUtil.isAdmin()) {
            crit.add(Restrictions.in("id", securityUtil.extractClientIds(sf.getCurrentSession())));
            if (securityUtil.extractClientIds(sf.getCurrentSession()).size() == 0)
                return ret;
        }

        for (ClientDO client : (List<ClientDO>) crit.list()) {
            Client c = new Client();
            c.copyFrom(client);
            ret.add(c);
        }
        for (Client c : ret)
            fillInEntryPointKeywordData(c);
        return ret;
    }


    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client')")
    public List<EntryPointDefinition> getEntryPointDefinitions() {
        return getEntryPointDefinitions(null);
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client')")
    public List<EntryPointDefinition> getEntryPointDefinitions(List<Long> clientIds) {
        List<EntryPointDefinition> ret = new ArrayList<EntryPointDefinition>();

        List<Long> allowedClientIds = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIds);
        boolean isAdmin = securityUtil.isAdmin();

        if (isAdmin ||
                allowedClientIds.size() > 0) {
            Criteria crit = sf.getCurrentSession().createCriteria(EntryPointDO.class);

            // Don't limit by client relationships if this is an admin
            if (!isAdmin) {
                crit.createAlias("clients", "clients");
                crit.add(Restrictions.in("clients.primaryKey", allowedClientIds));
            }

            // Get only distinct entry points
            crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            for (EntryPointDO entry : (List<EntryPointDO>) crit.list()) {
                EntryPointDefinition epd = new EntryPointDefinition();
                epd.copyFrom(entry);
                if (securityUtil.isAdmin())
                    epd.setCredentials(entry.getCredentials());
                else if (entry.getCredentials() != null)
                    epd.setCredentials("authorized");
                ret.add(epd);
            }

            for (EntryPointDefinition epd : ret)
                fillInEntryPointKeywordData(epd);
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasRole('ROLE_admin')")
    public EntryPointDefinition getEntryPointDefinition(EntryPointType type, String account) {
        EntryPointDefinition ret = new EntryPointDefinition();
        Criteria crit = sf.getCurrentSession().createCriteria(EntryPointDO.class);
        crit.add(Restrictions.eq("type", type));
        crit.add(Restrictions.eq("value", account));

        EntryPointDO result = (EntryPointDO) crit.uniqueResult();
        ret.copyFrom(result);

        if (securityUtil.isAdmin())
            ret.setCredentials(result.getCredentials());
        else if (result.getCredentials() != null)
            ret.setCredentials("authorized");

        fillInEntryPointKeywordData(ret);

        return ret;
    }

    public Client getClientById(long id) {
        if (!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), id))
            throw new SecurityException("Current user is not allowed to view specified client.");

        Client c = new Client();
        c.copyFrom((ClientDO) sf.getCurrentSession().get(ClientDO.class, id));
        fillInEntryPointKeywordData(c);
        return c;
    }

    private void fillInEntryPointKeywordData(Client c) {
        for (EntryPointDefinition epd : c.getEntryPoints())
            fillInEntryPointKeywordData(epd);
    }

    private void fillInEntryPointKeywordData(EntryPointDefinition epd) {
        Query q = sf.getCurrentSession().createQuery("select cep from CampaignEntryPointDO cep where cep.entryPoint=:epName and cep.type=:epType order by cep.keyword");
        q.setParameter("epName", epd.getValue());
        q.setParameter("epType", epd.getType());
        List<CampaignEntryPointDO> result = q.list();
        Map<String, String> keywords = new HashMap<String, String>();
        for (CampaignEntryPointDO row : result)
            keywords.put(row.getKeyword(), row.getCampaign().getUID());
        for (Keyword kywd : epd.getKeywords())
            if (keywords.containsKey(kywd.getKeyword()))
                kywd.setCampaignUID(keywords.get(kywd.getKeyword()));
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client')")
    @AuditEvent(AuditType.SaveClient)
    public Client save(Client client) {
        if (client == null)
            throw new IllegalArgumentException("Cannot save a null client.");

        ClientDO c = (ClientDO) sf.getCurrentSession().get(ClientDO.class, client.getClientId());
        if (c == null && securityUtil.isAdmin()) {
            c = new ClientDO();
        } else if (!securityUtil.getAllowedClientIDs(sf.getCurrentSession(), null).contains(client.getClientId())) {
            throw new SecurityException("You do not have permission to edit this client");
        }

        client.copyTo(c);
        EntryPointDO ep;
        if (client.getEntryPoints() != null) {
            c.getEntryPoints().clear();
            for (EntryPointDefinition epd : client.getEntryPoints()) {
                if (epd.getPrimaryKey() == null)
                    throw new IllegalArgumentException("Cannot assign an EntryPointDefinition to a client that has no primary key.  Define the entry point first.");
                ep = (EntryPointDO) sf.getCurrentSession().get(EntryPointDO.class, epd.getPrimaryKey());
                if (ep == null)
                    throw new IllegalArgumentException("Cannot assign an invalid EntryPointDefinition to a client.  pk='" + epd.getPrimaryKey() + "' not found.");
                c.getEntryPoints().add(ep);
            }
        }

        // Save client
        sf.getCurrentSession().persist(c);

        // Update client infos
        if (client.getClientInfos() != null) {
            for (ClientInfo info : client.getClientInfos()) {
                ClientInfoDO infoDO = null;
                if (info.getClientInfoId() != null)
                    infoDO = (ClientInfoDO) sf.getCurrentSession().get(ClientInfoDO.class, info.getClientInfoId());
                if (infoDO == null)
                    infoDO = new ClientInfoDO();

                infoDO.setClient(c);
                info.copyTo(infoDO);
                sf.getCurrentSession().persist(infoDO);

                if (c.getClientInfos() == null)
                    c.setClientInfos(new HashSet<ClientInfoDO>());
                c.getClientInfos().add(infoDO);
            }
        }

        // Update keyword limits
        if (client.getKeywordLimits() != null) {
            for (KeywordLimit kl : client.getKeywordLimits()) {
                KeywordLimitDO keyDO = null;
                if (kl.getKeywordLimitId() != null)
                    keyDO = (KeywordLimitDO) sf.getCurrentSession().get(KeywordLimitDO.class, kl.getKeywordLimitId());
                if (keyDO == null)
                    keyDO = new KeywordLimitDO();

                keyDO.setClient(c);
                kl.copyTo(keyDO);
                sf.getCurrentSession().persist(keyDO);
                if (c.getKeywordLimits() == null)
                    c.setKeywordLimits(new HashSet<KeywordLimitDO>());
                c.getKeywordLimits().add(keyDO);
            }
        }

        // Update add in messages
        if (client.getAddInMessages() != null) {
            for (AddInMessage add : client.getAddInMessages()) {

                // Get existing or new data object
                AddInMessageDO addDO = null;
                if (add.getAddInMessageId() != null)
                    addDO = (AddInMessageDO) sf.getCurrentSession().get(AddInMessageDO.class, add.getAddInMessageId());
                if (addDO == null) {
                    addDO = new AddInMessageDO();

                    // Add message to client list
                    if (c.getAddInMessages() == null)
                        c.setAddInMessages(new HashSet<AddInMessageDO>());
                    c.getAddInMessages().add(addDO);
                }
                // Make sure only admins are trying to modify ADMIN add in messages
                if ((addDO.getAddInMessageId() == null ||
                        !addDO.getMessage().equals(add.getMessage())) &&
                        add.getType() == AddInMessageType.ADMIN &&
                        !securityUtil.isAdmin())
                    throw new SecurityException("You do not have permission to create this type of add in message");

                // Update and persist object
                addDO.setClient(c);
                add.copyTo(addDO);
                sf.getCurrentSession().persist(addDO);

            }
        }

        sf.getCurrentSession().flush();

        // Requery client to send back up to date object
        c = (ClientDO) sf.getCurrentSession().get(ClientDO.class, c.getPrimaryKey());
        Client ret = new Client();
        ret.copyFrom(c);
        return ret;
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client') or hasRole('ROLE_account.manager')")
    public Keyword save(Keyword kwd) {
        if (kwd == null)
            throw new IllegalArgumentException("Cannot save a null keyword.");

        if (!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), kwd.getClientId()))
            throw new SecurityException("User is not allowed to create keywords for the specified client.");

        // Check that the keyword is available
        if (!checkKeywordAvailability(kwd))
            throw new FlexException("The keyword you have entered is currently unavailable");

        // Make sure adding this keyword won't put the client over their limit
        if (kwd.getPrimaryKey() == null) {
            ClientDO client = (ClientDO) sf.getCurrentSession().get(ClientDO.class, kwd.getClientId());
            EntryPointDO entryPoint = (EntryPointDO) sf.getCurrentSession().get(EntryPointDO.class, kwd.getEntryPointId());
            Integer max = DEFAULT_MAX_KEYWORDS;
            for (KeywordLimitDO limit : client.getKeywordLimits()) {
                if (limit.getEntryType().equals(entryPoint.getType())) {
                    max = limit.getMaxKeywords();
                    break;
                }
            }

            // Zero is infinite number of keywords
            if (max != 0) {
                // Find current count for given entry point type
                Criteria crit = sf.getCurrentSession().createCriteria(KeywordDO.class);
                crit.add(Restrictions.eq("client.primaryKey", kwd.getClientId()));
                crit.createAlias("entryPoint", "entryPoint");
                crit.add(Restrictions.eq("entryPoint.type", entryPoint.getType()));
                crit.setProjection(Projections.rowCount());
                Integer currentCount = (Integer) crit.uniqueResult();
                if (currentCount >= max)
                    throw new FlexException("You have already reached your maximum " + max +
                            " keywords for " + entryPoint.getType() + " accounts.");
            }
        }

        KeywordDO kwdData = null;
        if (kwd.getPrimaryKey() != null)
            kwdData = (KeywordDO) sf.getCurrentSession().get(KeywordDO.class, kwd.getPrimaryKey());
        if (kwdData == null)
            kwdData = new KeywordDO();
        kwd.copyTo(kwdData);
        kwdData.setClient((ClientDO) sf.getCurrentSession().get(ClientDO.class, kwd.getClientId()));
        kwdData.setEntryPoint((EntryPointDO) sf.getCurrentSession().get(EntryPointDO.class, kwd.getEntryPointId()));
        sf.getCurrentSession().persist(kwdData);
        sf.getCurrentSession().flush();
        Keyword ret = new Keyword();
        ret.copyFrom(kwdData);
        return ret;
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client')")
    public EntryPointDefinition save(EntryPointDefinition epd) {
        if (epd == null)
            throw new IllegalArgumentException("Cannot save a null entry point definition.");

        // Make sure they have access to this client
        List<Long> allowedClientIds = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), null);
        if (epd.getClientIDs() != null) {
            for (int clientId : epd.getClientIDs()) {
                if (!allowedClientIds.contains(new Long(clientId))) {
                    throw new SecurityException("You do not have permission to edit entry points for these clients");
                }
            }
        }

        EntryPointDO ep = null;
        if (epd.getPrimaryKey() != null)
            ep = (EntryPointDO) sf.getCurrentSession().get(EntryPointDO.class, epd.getPrimaryKey());
        if (ep == null)
            ep = new EntryPointDO();
        epd.copyTo(ep);

        Set<ClientDO> newClientList = new HashSet<ClientDO>();
        if (epd.getClientIDs() != null) {
            for (Integer id : epd.getClientIDs())
                newClientList.add((ClientDO) sf.getCurrentSession().get(ClientDO.class, id.longValue()));

            ep.getClients().retainAll(newClientList);
            ep.getClients().addAll(newClientList);
        }

        sf.getCurrentSession().persist(ep);
        sf.getCurrentSession().flush();
        EntryPointDefinition ret = new EntryPointDefinition();
        ret.copyFrom(ep);
        return ret;
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client')")
    public List<Keyword> getAllKeywords() {
        return getKeywords(null);
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_client')")
    public List<Keyword> getKeywords(List<Long> clientIds) {
        List<Keyword> ret = new ArrayList<Keyword>();
        List<Long> allowedClientIds = securityUtil.getAllowedClientIDs(sf.getCurrentSession(), clientIds);

        if (allowedClientIds.size() > 0) {
            Criteria crit = sf.getCurrentSession().createCriteria(KeywordDO.class);
            crit.add(Restrictions.in("client.primaryKey", allowedClientIds));

            Keyword temp;
            for (KeywordDO keyword : (List<KeywordDO>) crit.list()) {
                temp = new Keyword();
                temp.copyFrom(keyword);
                ret.add(temp);
            }
        }
        return ret;
    }

    public List<Keyword> getAllKeywordsForClient(Long clientID) {
        if (clientID == null)
            throw new IllegalArgumentException("Must provide a client ID to query on.");
        Query q = sf.getCurrentSession().createQuery("select k from KeywordDO k where k.client.id=:id");
        q.setParameter("id", clientID);
        List<Keyword> ret = new ArrayList<Keyword>();
        Keyword temp;
        for (KeywordDO keyword : (List<KeywordDO>) q.list()) {
            temp = new Keyword();
            temp.copyFrom(keyword);
            ret.add(temp);
        }
        return ret;
    }

    public List<Keyword> getAllKeywordsForClientAndEntryPoint(Long clientID, Long entryPointID) {
        if (clientID == null)
            throw new IllegalArgumentException("Must provide a client ID to query on.");
        if (entryPointID == null)
            throw new IllegalArgumentException("Must provide an Entry Point ID to query on.");

        Query q = sf.getCurrentSession().createQuery("select k from KeywordDO k where k.client.id=:clientID and k.entryPoint.id=:entryID");
        q.setParameter("clientID", clientID);
        q.setParameter("entryID", entryPointID);
        List<Keyword> ret = new ArrayList<Keyword>();
        Keyword temp;
        for (KeywordDO keyword : (List<KeywordDO>) q.list()) {
            temp = new Keyword();
            temp.copyFrom(keyword);
            ret.add(temp);
        }
        return ret;
    }

    public List<Keyword> getAllKeywordsForEntryPoint(Long entryPointID) {
        if (entryPointID == null)
            throw new IllegalArgumentException("Must provide an Entry Point ID to query on");
        Query q = sf.getCurrentSession().createQuery("select k from KeywordDO k where k.entryPoint.id=:id");
        q.setParameter("id", entryPointID);
        List<Keyword> ret = new ArrayList<Keyword>();
        Keyword temp;
        for (KeywordDO keyword : (List<KeywordDO>) q.list()) {
            temp = new Keyword();
            temp.copyFrom(keyword);
            ret.add(temp);
        }
        return ret;
    }

    @PreAuthorize("hasRole('ROLE_admin') or hasRole('ROLE_account.manager')")
    public void delete(Keyword kwd) {
        if (kwd == null)
            throw new IllegalArgumentException("Cannot save a null keyword.");

        if (!userManager.isUserAllowedForClientId(securityUtil.getPrincipalName(), kwd.getClientId()))
            throw new SecurityException("User is not allowed to delete keywords for the specified client.");

        KeywordDO kwdData = null;
        if (kwd.getPrimaryKey() != null)
            kwdData = (KeywordDO) sf.getCurrentSession().get(KeywordDO.class, kwd.getPrimaryKey());

        Query q = sf.getCurrentSession().getNamedQuery("find.matching.entry.point");
        q.setParameter("entryPoint", kwdData.getEntryPoint().getValue());
        q.setParameter("keyword", kwdData.getKeyword());
        q.setParameter("type", kwdData.getEntryPoint().getType());
        CampaignEntryPointDO cep = (CampaignEntryPointDO) q.uniqueResult();
        if (cep != null)
            throw new IllegalArgumentException("Cannot delete this keyword, as it is still in use in campaign UID='" + cep.getCampaign().getUID() + "'");

        sf.getCurrentSession().delete(kwdData);
    }

    public List<ReservedKeyword> getAllReservedKeywords() {
        List<ReservedKeyword> ret = new ArrayList<ReservedKeyword>();
        Criteria crit = sf.getCurrentSession().createCriteria(ReservedKeywordDO.class);

        for (ReservedKeywordDO keyword : (List<ReservedKeywordDO>) crit.list()) {
            ReservedKeyword key = new ReservedKeyword();
            key.copyFrom(keyword);
            ret.add(key);
        }
        return ret;
    }

    public ReservedKeyword save(ReservedKeyword keyword) {
        ReservedKeywordDO keyDO = null;

        if (keyword.getReservedKeywordId() != null)
            keyDO = (ReservedKeywordDO) sf.getCurrentSession().get(ReservedKeywordDO.class, keyword.getReservedKeywordId());
        if (keyDO == null)
            keyDO = new ReservedKeywordDO();

        keyword.copyTo(keyDO);
        sf.getCurrentSession().persist(keyDO);

        ReservedKeyword ret = new ReservedKeyword();
        ret.copyFrom(keyDO);
        return ret;
    }

    public void delete(ReservedKeyword keyword) {
        ReservedKeywordDO keyDO = (ReservedKeywordDO) sf.getCurrentSession().get(ReservedKeywordDO.class, keyword.getReservedKeywordId());
        if (keyDO != null)
            sf.getCurrentSession().delete(keyDO);
    }

    public Boolean checkKeywordAvailability(Keyword keyword) {
        // A keyword is required
        if (keyword == null ||
                keyword.getKeyword() == null ||
                keyword.getKeyword().length() == 0)
            return false;

        // Make sure the keyword is not on the reserve list
        Criteria crit = sf.getCurrentSession().createCriteria(ReservedKeywordDO.class);
        crit.add(Restrictions.eq("keyword", keyword.getKeyword()));
        crit.setProjection(Projections.count("keyword"));
        Long reservedCount = (Long) crit.uniqueResult();
        if (reservedCount > 0)
            return false;

        // Make sure this keyword is not used by this entry point already
        crit = sf.getCurrentSession().createCriteria(KeywordDO.class);
        crit.add(Restrictions.eq("keyword", keyword.getKeyword()));
        crit.add(Restrictions.eq("entryPoint.primaryKey", keyword.getEntryPointId()));
        crit.setProjection(Projections.count("keyword"));
        Long usedCount = (Long) crit.uniqueResult();
        if (usedCount > 0)
            return false;

        return true;
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    public void disableClient(Long clientID) {
        if (clientID == null)
            return;
        ClientDO client = (ClientDO) sf.getCurrentSession().get(ClientDO.class, clientID);
        if (client == null)
            return;
        client.setActive(false);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    public void enableClient(Long clientID) {
        if (clientID == null)
            return;
        ClientDO client = (ClientDO) sf.getCurrentSession().get(ClientDO.class, clientID);
        if (client == null)
            return;
        client.setActive(true);
    }

    public String startTwitterAuth(String callbackURL) {
        return "https://www.twitter.com/oauth/authorize?oauth_token=" + twitterCoordinator.acquireRequestToken(callbackURL).getToken();
    }

    public String authTwitterAccount(String oauthToken, String oauthVerifier) {
        if (twitterCoordinator.retrieveAccessToken(oauthToken, oauthVerifier))
            return "<html><body><h3>You have been successfully authorized!</h3></body></html>";
        else
            return "<html><body><h3>Your authorization failed.  Please contact the administrator for help.</h3></body></html>";
    }
}
