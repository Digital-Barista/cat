package com.digitalbarista.cat.audit;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.BeanUtils;

public class AuditInterceptor {

	private static final String dateFormat="MM/dd/yyyy HH:mm:ss.SSS";
	
	@Resource
	private SessionContext ctx;
	
	@PersistenceContext(unitName="cat-data")
	private EntityManager em;
	
	@AroundInvoke
	public Object mdbInterceptor(InvocationContext iCtx) throws Exception
	{
		AuditEvent eventAnnotation = iCtx.getMethod().getAnnotation(AuditEvent.class);
		if(eventAnnotation==null)
			return iCtx.proceed();
		
		Auditable target = null;
		for(Object obj : iCtx.getParameters())
		{
			if(Auditable.class.isAssignableFrom(obj.getClass()))
			{
				target = (Auditable)obj;
				break;
			}
		}
		
		if(target==null)
			return iCtx.proceed();
		
		Object d1Obj=null;
		Object d2Obj=null;
		
		boolean foundD1=false;
		boolean foundD2=false;
		
		for(Field f : target.getClass().getDeclaredFields())
		{
			try
			{
				f.setAccessible(true);
				if(!foundD1 && f.getAnnotation(PrimaryDescriminator.class)!=null)
				{
					d1Obj = f.get(target);
					foundD1=true;
				}
				if(!foundD2 && f.getAnnotation(SecondaryDescriminator.class)!=null)
				{
					d2Obj = f.get(target);
					foundD2=true;
				}
				if(foundD1 && foundD2)
					break;
			}
			catch(Exception e)
			{
				throw e;
			}
			finally
			{
				try{f.setAccessible(false);}catch(Throwable t){}
			}
		}
		
		if(target.getClass().getSuperclass()!=null)
		{
			for(Field f : target.getClass().getSuperclass().getDeclaredFields())
			{
				try
				{
					f.setAccessible(true);
					if(!foundD1 && f.getAnnotation(PrimaryDescriminator.class)!=null)
					{
						d1Obj = f.get(target);
						foundD1=true;
					}
					if(!foundD2 && f.getAnnotation(SecondaryDescriminator.class)!=null)
					{
						d2Obj = f.get(target);
						foundD2=true;
					}
					if(foundD1 && foundD2)
						break;
				}
				catch(Exception e)
				{
					throw e;
				}
				finally
				{
					try{f.setAccessible(false);}catch(Throwable t){}
				}
			}
		}
		
		if(foundD1 && foundD2)
		{
			PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(target.getClass());
			for(PropertyDescriptor desc : pds)
			{
				try
				{
					desc.getReadMethod().setAccessible(true);
					if(!foundD1 &&
					   (desc.getReadMethod().getAnnotation(PrimaryDescriminator.class)!=null ||
					   desc.getWriteMethod().getAnnotation(PrimaryDescriminator.class)!=null))
					{
						d1Obj = desc.getReadMethod().invoke(target);
						foundD1=true;
					}
						
					if(!foundD2 &&
					   (desc.getReadMethod().getAnnotation(SecondaryDescriminator.class)!=null ||
					   desc.getWriteMethod().getAnnotation(SecondaryDescriminator.class)!=null))
					{
						d2Obj = desc.getReadMethod().invoke(target);
						foundD2=true;
					}
					
					if(foundD1 && foundD2)
						break;
				}
				catch(Exception e)
				{
					throw e;
				}
				finally
				{
					try{desc.getReadMethod().setAccessible(false);}catch(Throwable t){}
				}
			}
		}
		
		AuditDO audit = new AuditDO();
		audit.setAuditType(eventAnnotation.value());
		audit.setUsername(ctx.getCallerPrincipal().getName());
		audit.setTimestamp(new Date());
		audit.setDescriminator1(makeString(d1Obj));
		audit.setDescriminator2(makeString(d2Obj));
		audit.setData(target.auditString());
		em.persist(audit);
		return iCtx.proceed();
	}
	
	private String makeString(Object obj)
	{
		if(obj==null)
			return null;
		
		if(obj instanceof String)
			return (String)obj;
		
		if(obj instanceof Date)
			return new SimpleDateFormat(dateFormat).format((Date)obj);
		
		return obj.toString();
	}
}
