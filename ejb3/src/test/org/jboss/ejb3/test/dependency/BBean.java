package org.jboss.ejb3.test.dependency;

import javax.ejb.Remote;
import javax.ejb.Stateless;

@Stateless(name="ejb/B")
@Remote(B.class)
public class BBean implements B {

	public String sayHello() {
		return "Hello";
	}

}
