package org.jboss.ejb3.test.dependency;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.jboss.annotation.ejb.Depends;
import org.jboss.logging.Logger;

@Stateless(name="ejb/A")
@Remote(A.class)
public class ABean implements A {

	private static final Logger log = Logger.getLogger(ABean.class);

	@Depends("jboss.j2ee:jar=B.jar,name=ejb/B,service=EJB3")
	@EJB
	private B bInstance; 
	
	public String getHelloB() {
		return bInstance.sayHello();
	}
}
