package com.digitalbarista.cat.util;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

import com.digitalbarista.cat.business.PagingInfo;

public class PagingUtil {

	public static final Integer DEFAULT_PAGE_SIZE = 250;
	
	public static void applyPagingInfo(Criteria crit, PagingInfo info)
	{
		if (info != null)
		{
			// Apply paging
			Integer pageIndex = info.getPageIndex();
			Integer pageSize = info.getPageSize();
			
			if (pageIndex == null ||
				pageIndex < 0)
				pageIndex = 0;
			
			if (pageSize == null ||
				pageSize < 0)
				pageSize = DEFAULT_PAGE_SIZE;
			
			Integer firstResult = pageIndex * pageSize;
			crit.setFirstResult(firstResult);
			crit.setMaxResults(pageSize);
			
			// Apply sort
			if (info.getSortProperty() != null)
			{
				Order direction = null;
				if (info.getSortDirectionAscending())
					direction = Order.asc(info.getSortProperty());
				else
					direction = Order.desc(info.getSortProperty());
				
				crit.addOrder(direction);
			}
		}
	}
	
	public static Integer getTotalResultCount(Criteria crit)
	{
		Integer ret = 0;
		if (crit != null)
		{
			crit.setProjection(Projections.rowCount());
			ret = (Integer)crit.uniqueResult();
			
			// Remove projection
			crit.setProjection(null);
			crit.setResultTransformer(Criteria.ROOT_ENTITY);
		}
		return ret;
	}
}
