package com.digitalbarista.cat.ejb.session;
import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.Client;
import com.digitalbarista.cat.business.EntryPointDefinition;
import com.digitalbarista.cat.business.Keyword;

@Local
public interface ClientManager {
	public Client getClientById(long id);
	public List<Client> getVisibleClients();
	public List<EntryPointDefinition> getEntryPointDefinitions();
	public Client save(Client client);
	public EntryPointDefinition save(EntryPointDefinition epd);
	public Keyword save(Keyword kwd);
	public void delete(Keyword kwd);
	public List<Keyword> getAllKeywords();
	public List<Keyword> getAllKeywordsForEntryPoint(Long entryPointID);
	public List<Keyword> getAllKeywordsForClient(Long clientID);
	public List<Keyword> getAllKeywordsForClientAndEntryPoint(Long clientID, Long entryPointID);
}
