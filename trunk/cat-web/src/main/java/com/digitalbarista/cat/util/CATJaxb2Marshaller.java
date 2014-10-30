/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.digitalbarista.cat.util;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.log4j.LogManager;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 *
 * @author Falken
 */
public class CATJaxb2Marshaller extends Jaxb2Marshaller {
  public CATJaxb2Marshaller()
  {
    super();
    Set<URL> urls = ClasspathHelper.forPackage("com.digitalbarista.cat.business",getClass().getClassLoader());
    Set<URL> finalURLs = new HashSet<URL>();
    for(URL url : urls)
    {
      if(url.getProtocol().equalsIgnoreCase("vfsfile"))
      {
        try
        {
          finalURLs.add(new URL(url.toString().replaceAll("vfsfile:", "file:")));
        }
        catch(Exception e)
        {
          LogManager.getLogger(getClass()).warn("Could not convert a VFS URL for class scanning:"+url.toString(),e);
        }
      } else
      {
        finalURLs.add(url);
      }
    }
    Reflections refl = new Reflections(new ConfigurationBuilder()
                  .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.digitalbarista.cat.business")))
                  .setUrls(finalURLs)
                  .setScanners(new TypeAnnotationsScanner()));
    Set<Class<?>> jaxbClasses = refl.getTypesAnnotatedWith(XmlRootElement.class);
    jaxbClasses.addAll(refl.getTypesAnnotatedWith(XmlType.class));
    Class<?>[] classArray = new Class<?>[jaxbClasses.size()];
    super.setClassesToBeBound(jaxbClasses.toArray(classArray));
  }

  @Override
  public void setClassesToBeBound(Class<?>... classesToBeBound) {
    //Do nothing . . . this class scans for the classes that need to be bound.
  }
}
