package com.digitalbarista.cat.util;

import com.digitalbarista.cat.business.ServiceMetadata;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;

/**
 * User: Kris
 * Date: 3/20/13
 * Time: 3:53 PM
 */
public class CriteriaUtil {
    public static void applyPagingInfo(Criteria crit, ServiceMetadata meta)
    {
        if (meta != null)
        {
            // Apply paging
            crit.setFirstResult(meta.getOffset());
            crit.setMaxResults(meta.getLimit());

            // Apply sort
            if (meta.getSortField() != null)
            {
                Order direction = null;
                if (meta.getSortDirection() > 0)
                    direction = Order.asc(meta.getSortField());
                else
                    direction = Order.desc(meta.getSortField());

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
            Object result = crit.uniqueResult();
            ret = result != null ? ((Long)result).intValue() : 0;

            // Remove projection
            crit.setProjection(null);
            crit.setResultTransformer(Criteria.ROOT_ENTITY);
        }
        return ret;
    }
}
