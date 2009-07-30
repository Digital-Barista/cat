package com.digitalbarista.cat.ejb.session;

import java.util.List;

import javax.ejb.Local;

import com.digitalbarista.cat.business.LayoutInfo;
import com.digitalbarista.cat.data.LayoutInfoDO;

@Local
public interface LayoutManager {
	public List<LayoutInfo> getLayoutInfo(List<String> uidList);
	public LayoutInfo getLayoutInfo(String uid);
	public LayoutInfoDO getSimpleLayoutInfo(String uuid);
	public void delete(String uid);
	public List<LayoutInfo> getLayoutsByCampaign(String uid);
	public List<LayoutInfo> getLayoutsByCampaignAndVersion(String uid,Integer version);
	public void save(LayoutInfo layout);
}
