FIELD-level http session replication example
  Ben Wang (ben dot wang at jboss dot com)
  and Brian Stansberry (bstansberry at jboss dot com)
  03-06

This is an example that illustrates how to use the FIELD replication
granularity to replicate state information about
students and the courses they take over a JBoss AS 4.0.4 or higher cluster (I am
currently running it using 4.0.4.CR2). In addition,
the application is run with the JBoss scoped classloader. If you don't
need the scoped classloader, you can comment it out from jboss-web.xml (in etc/WEB-INF).

In this example, where necessary the domain model classes have been
annotated with "@@org.jboss.web.tomcat.service.session.AopMarker" and
"@@org.jboss.web.tomcat.service.session.InstanceOfAopMarker" (PojoCache JDK1.4
annotations) where "InstanceOfAopMarker" signifies that every subclass will be
instrumented as well, e.g., no need for Student to annotate again). 

To run the example, the annoc pre-compiler needs to be executed first to
compile the annotations. Once annoc has been run, aopc is used to
instrument the classes in the domain model before the example is run
(such that you don't need a special class loader during start up). Note
that both annoc and aopc targets all have been bundled into dist target
in build.xml. Please refer it there for details.

The example involves 4 domain model classes: Person, Student, Address
and Course where Student is a subclass of Person. A Person has an Address,
while a Student can be registered for 0 or more Courses (use of Collection
here).

Note that in order to run this example, you will need to have run JBoss
web clustering before (e.g., know how to use a load balancer, failover, and sticky sessions).
If you have not done that, please refer to
http://wiki.jboss.org/wiki/Wiki.jsp?page=JBossHA. Please refer to:
 http://wiki.jboss.org/wiki/Wiki.jsp?page=ConfiguringMultipleJBossInstancesOnOneMachine
for info on how to setup two separate JBoss instances on the same machine,
and also go through the "Web Clustering" section, specifically, this wiki:
http://wiki.jboss.org/wiki/Wiki.jsp?page=UsingMod_jk1.2WithJBoss. It will
be a little involved to set it up first to make sure sticky sessions and
loadbalancing are working correctly. But if you use a HW load balancer, I think
life will be easier for you (well, to some extent).

Also, note that this example requires Ant 1.6.5 and higher to run, and the JDK required is 1.4.2 and up.

To run it:

1) build the war file (by typing "ant dist -Djboss.config=XXX") where
"XXX" is the path to your jboss config directory (e.g. /home/jdoe/jboss-4.0.4.GA/server/all). That is,
you need to define jboss.config first. (Note that if you have spaces in
your jboss.config path, you can enclose it with "".) This will produce
a war file called test-http-scoped-FIELD.war under the dist directory.

Note that we have bundled a third-party library (qdox.jar) under "lib"
directory to support JDK1.4 annotation. This is used for the "annoc" target
only; it is not needed at runtime.

2) Copy this war file to your jboss clustered deploy directories, an equivalent
 of "all/deploy", e.g.). In my case, they are node0 and node1 (and the jvmRoute that
 I assigned for jk loadbalancer are "node0" and "node1" as well).

3) Start up your 2 JBoss instances. For example, in my case,
"run.sh -c node0 -b $MYTESTIP_1" and "run.sh -c node1 -b $MYTESTIP_2".

4) The relevant urls are:
  http://hostX/test-http-scoped-FIELD/setSession.jsp
  http://hostX/test-http-scoped-FIELD/modifyAttribute.jsp
  http://hostX/test-http-scoped-FIELD/getAttribute.jsp
where hostX points to the loadbalancer, e.g., Apache.

You can examine the jsps under the etc directory. Basically, setSession.jsp
tries to store the POJOS in the http session using session.setAttribute(),
modifyAttribute.jsp then tries to modify it, and finally getAttribute.jsp can
retrieve the POJOs.

To illustrate the fine-grained replication, here is what I run (assuming we have
clustered node of 0 & 1):
a) setSession.jsp (should go to node0)
b) getAttribute.jsp (go to node0)
c) kill node0 (so the next request will failover to node1)
d) getAttribute.jsp to validate the values (go to node1 now)
e) modifyAttribute.jsp to modify the POJOs (go to node1)
f) restart node0 to join the cluster
g) kill node1 so it will failover back to node0
h) getAttribute.jsp (go to node0 now)

Note that you can check the session id for the jvmRoute suffix to check which
node the request went to.

When you get to this far, you can also modify the jsps and experiment further!

To help make it easier to use annoc and aopc in your own builds, the example uses an 
Ant script PojoCacheTasks.xml that is an independent declaration
of the aopc and annoc targets.  A copy of this file can be directly imported into to your 
project's build using the ant <import/> task. Take a look at the example's build.xml 
to see how this can be used.

Please report problems to the Clustering user forum:
http://www.jboss.com/index.html?module=bb&op=viewforum&f=64
