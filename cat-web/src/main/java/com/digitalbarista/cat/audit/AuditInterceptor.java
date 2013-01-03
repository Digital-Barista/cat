package com.digitalbarista.cat.audit;

import com.digitalbarista.cat.util.SecurityUtil;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.SessionFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("auditInterceptor")
@Lazy
public class AuditInterceptor implements MethodInterceptor {

	private static final String dateFormat="MM/dd/yyyy HH:mm:ss.SSS";
		
  @Autowired
  private SessionFactory sf;
  
  @Autowired
  private SecurityUtil securityUtil;

	public Object invoke(MethodInvocation mi) throws Throwable
	{
		AuditEvent eventAnnotation = mi.getMethod().getAnnotation(AuditEvent.class);
		if(eventAnnotation==null)
			return mi.proceed();
		
		Auditable target = null;
		for(Object obj : mi.getArguments())
		{
			if(Auditable.class.isAssignableFrom(obj.getClass()))
			{
				target = (Auditable)obj;
				break;
			}
		}
		
		if(target==null)
			return mi.proceed();
		
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
		audit.setUsername(securityUtil.getPrincipalName());
		audit.setTimestamp(new Date());
		audit.setDescriminator1(makeString(d1Obj));
		audit.setDescriminator2(makeString(d2Obj));
		audit.setData(target.auditString());
		sf.getCurrentSession().persist(audit);
		return mi.proceed();
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
