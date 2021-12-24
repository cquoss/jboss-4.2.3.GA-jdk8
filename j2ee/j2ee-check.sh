#! /bin/bash
#
# This script does javap on classes that are in sun's j2ee-1_4.jar and writes the result to a file.
#
# To verify our implementation of those classes, do the following
#
#  j2ee-check jboss
#  j2ee-check j2ee
#  diff jboss.out j2ee.out
#
# Ideally, the files should be identical.
# See the bottom of this script for files that are not yet included.
#
# author: thomas.diesler@jboss.org
#
# $Id: j2ee-check.sh 40020 2006-01-13 08:45:43Z tdiesler $

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# For Cygwin, switch paths to unix format
if $cygwin; then
    JBOSS_HOME=`cygpath --path --unix "$JBOSS_HOME"`
    J2EE_HOME=`cygpath --path --unix "$J2EE_HOME"`
fi

# set j2ee classpath
J2EE_CLASSPATH="$J2EE_CLASSPATH:$J2EE_HOME/lib/j2ee.jar"

# set jboss classpath
JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/lib/namespace.jar"
JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/server/all/lib/jboss-j2ee.jar"
JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/server/all/lib/jboss-jsr77.jar"
JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/server/all/lib/jboss-management.jar"
JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/server/all/lib/jboss-jaxrpc.jar"
JBOSS_CLASSPATH="$JBOSS_CLASSPATH:$JBOSS_HOME/server/all/lib/jboss-saaj.jar"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    J2EE_CLASSPATH=`cygpath --path --windows "$J2EE_CLASSPATH"`
    JBOSS_CLASSPATH=`cygpath --path --windows "$JBOSS_CLASSPATH"`
fi

# set the default
JAVAP_CLASSPATH=$JBOSS_CLASSPATH
mode=jboss

# set for J2EE RI
if [ "$1" = "j2ee" ]; then
   JAVAP_CLASSPATH=$J2EE_CLASSPATH
   mode=j2ee
fi

echo "JBOSS_HOME = $JBOSS_HOME"
echo "J2EE_HOME = $J2EE_HOME"

# echo "CLASSPATH: $JAVAP_CLASSPATH"

javap -protected -classpath $JAVAP_CLASSPATH \
   javax.ejb.AccessLocalException \
   javax.ejb.CreateException \
   javax.ejb.DuplicateKeyException \
   javax.ejb.EJBContext \
   javax.ejb.EJBException \
   javax.ejb.EJBHome \
   javax.ejb.EJBLocalHome \
   javax.ejb.EJBLocalObject \
   javax.ejb.EJBMetaData \
   javax.ejb.EJBObject \
   javax.ejb.EnterpriseBean \
   javax.ejb.EntityBean \
   javax.ejb.EntityContext \
   javax.ejb.FinderException \
   javax.ejb.Handle \
   javax.ejb.HomeHandle \
   javax.ejb.MessageDrivenBean \
   javax.ejb.MessageDrivenContext \
   javax.ejb.NoSuchEntityException \
   javax.ejb.NoSuchObjectLocalException \
   javax.ejb.ObjectNotFoundException \
   javax.ejb.RemoveException \
   javax.ejb.SessionBean \
   javax.ejb.SessionContext \
   javax.ejb.SessionSynchronization \
   javax.ejb.TimedObject \
   javax.ejb.Timer \
   javax.ejb.TimerHandle \
   javax.ejb.TimerService \
   javax.ejb.TransactionRequiredLocalException \
   javax.ejb.TransactionRolledbackLocalException \
   javax.ejb.spi.HandleDelegate \
   javax.enterprise.deploy.model.DDBean \
   javax.enterprise.deploy.model.DDBeanRoot \
   javax.enterprise.deploy.model.DeployableObject \
   javax.enterprise.deploy.model.J2eeApplicationObject \
   javax.enterprise.deploy.model.XpathEvent \
   javax.enterprise.deploy.model.XpathListener \
   javax.enterprise.deploy.model.exceptions.DDBeanCreateException \
   javax.enterprise.deploy.shared.ActionType \
   javax.enterprise.deploy.shared.CommandType \
   javax.enterprise.deploy.shared.DConfigBeanVersionType \
   javax.enterprise.deploy.shared.ModuleType \
   javax.enterprise.deploy.shared.StateType \
   javax.enterprise.deploy.shared.factories.DeploymentFactoryManager \
   javax.enterprise.deploy.spi.DConfigBean \
   javax.enterprise.deploy.spi.DConfigBeanRoot \
   javax.enterprise.deploy.spi.DeploymentConfiguration \
   javax.enterprise.deploy.spi.DeploymentManager \
   javax.enterprise.deploy.spi.Target \
   javax.enterprise.deploy.spi.TargetModuleID \
   javax.enterprise.deploy.spi.exceptions.BeanNotFoundException \
   javax.enterprise.deploy.spi.exceptions.ClientExecuteException \
   javax.enterprise.deploy.spi.exceptions.ConfigurationException \
   javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException \
   javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException \
   javax.enterprise.deploy.spi.exceptions.InvalidModuleException \
   javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException \
   javax.enterprise.deploy.spi.exceptions.TargetException \
   javax.enterprise.deploy.spi.factories.DeploymentFactory \
   javax.enterprise.deploy.spi.status.ClientConfiguration \
   javax.enterprise.deploy.spi.status.DeploymentStatus \
   javax.enterprise.deploy.spi.status.ProgressEvent \
   javax.enterprise.deploy.spi.status.ProgressListener \
   javax.enterprise.deploy.spi.status.ProgressObject \
   javax.jms.BytesMessage \
   javax.jms.Connection \
   javax.jms.ConnectionConsumer \
   javax.jms.ConnectionFactory \
   javax.jms.ConnectionMetaData \
   javax.jms.DeliveryMode \
   javax.jms.Destination \
   javax.jms.ExceptionListener \
   javax.jms.IllegalStateException \
   javax.jms.InvalidClientIDException \
   javax.jms.InvalidDestinationException \
   javax.jms.InvalidSelectorException \
   javax.jms.JMSException \
   javax.jms.JMSSecurityException \
   javax.jms.MapMessage \
   javax.jms.Message \
   javax.jms.MessageConsumer \
   javax.jms.MessageEOFException \
   javax.jms.MessageFormatException \
   javax.jms.MessageListener \
   javax.jms.MessageNotReadableException \
   javax.jms.MessageNotWriteableException \
   javax.jms.MessageProducer \
   javax.jms.ObjectMessage \
   javax.jms.Queue \
   javax.jms.QueueBrowser \
   javax.jms.QueueConnection \
   javax.jms.QueueConnectionFactory \
   javax.jms.QueueReceiver \
   javax.jms.QueueRequestor \
   javax.jms.QueueSender \
   javax.jms.QueueSession \
   javax.jms.ResourceAllocationException \
   javax.jms.ServerSession \
   javax.jms.ServerSessionPool \
   javax.jms.Session \
   javax.jms.StreamMessage \
   javax.jms.TemporaryQueue \
   javax.jms.TemporaryTopic \
   javax.jms.TextMessage \
   javax.jms.Topic \
   javax.jms.TopicConnection \
   javax.jms.TopicConnectionFactory \
   javax.jms.TopicPublisher \
   javax.jms.TopicRequestor \
   javax.jms.TopicSession \
   javax.jms.TopicSubscriber \
   javax.jms.TransactionInProgressException \
   javax.jms.TransactionRolledBackException \
   javax.jms.XAConnection \
   javax.jms.XAConnectionFactory \
   javax.jms.XAQueueConnection \
   javax.jms.XAQueueConnectionFactory \
   javax.jms.XAQueueSession \
   javax.jms.XASession \
   javax.jms.XATopicConnection \
   javax.jms.XATopicConnectionFactory \
   javax.jms.XATopicSession \
   javax.management.Attribute \
   javax.management.AttributeChangeNotification \
   javax.management.AttributeChangeNotificationFilter \
   javax.management.AttributeList \
   javax.management.AttributeNotFoundException \
   javax.management.AttributeValueExp \
   javax.management.BadAttributeValueExpException \
   javax.management.BadBinaryOpValueExpException \
   javax.management.BadStringOperationException \
   javax.management.Descriptor \
   javax.management.DescriptorAccess \
   javax.management.DynamicMBean \
   javax.management.InstanceAlreadyExistsException \
   javax.management.InstanceNotFoundException \
   javax.management.IntrospectionException \
   javax.management.InvalidApplicationException \
   javax.management.InvalidAttributeValueException \
   javax.management.JMException \
   javax.management.JMRuntimeException \
   javax.management.ListenerNotFoundException \
   javax.management.MBeanAttributeInfo \
   javax.management.MBeanConstructorInfo \
   javax.management.MBeanException \
   javax.management.MBeanFeatureInfo \
   javax.management.MBeanInfo \
   javax.management.MBeanNotificationInfo \
   javax.management.MBeanOperationInfo \
   javax.management.MBeanParameterInfo \
   javax.management.MBeanPermission \
   javax.management.MBeanRegistration \
   javax.management.MBeanRegistrationException \
   javax.management.MBeanServer \
   javax.management.MBeanServerBuilder \
   javax.management.MBeanServerConnection \
   javax.management.MBeanServerDelegate \
   javax.management.MBeanServerDelegateMBean \
   javax.management.MBeanServerFactory \
   javax.management.MBeanServerInvocationHandler \
   javax.management.MBeanServerNotification \
   javax.management.MBeanServerPermission \
   javax.management.MBeanTrustPermission \
   javax.management.MalformedObjectNameException \
   javax.management.MatchQueryExp \
   javax.management.NotCompliantMBeanException \
   javax.management.NotQueryExp \
   javax.management.Notification \
   javax.management.NotificationBroadcaster \
   javax.management.NotificationBroadcasterSupport \
   javax.management.NotificationEmitter \
   javax.management.NotificationFilter \
   javax.management.NotificationFilterSupport \
   javax.management.NotificationListener \
   javax.management.ObjectInstance \
   javax.management.ObjectName \
   javax.management.OperationsException \
   javax.management.OrQueryExp \
   javax.management.PersistentMBean \
   javax.management.QualifiedAttributeValueExp \
   javax.management.Query \
   javax.management.QueryEval \
   javax.management.QueryExp \
   javax.management.ReflectionException \
   javax.management.RuntimeErrorException \
   javax.management.RuntimeMBeanException \
   javax.management.RuntimeOperationsException \
   javax.management.ServiceNotFoundException \
   javax.management.StandardMBean \
   javax.management.StringValueExp \
   javax.management.ValueExp \
   javax.management.j2ee.ListenerRegistration \
   javax.management.j2ee.Management \
   javax.management.j2ee.ManagementHome \
   javax.management.j2ee.statistics.BoundaryStatistic \
   javax.management.j2ee.statistics.BoundedRangeStatistic \
   javax.management.j2ee.statistics.CountStatistic \
   javax.management.j2ee.statistics.EJBStats \
   javax.management.j2ee.statistics.EntityBeanStats \
   javax.management.j2ee.statistics.JCAConnectionPoolStats \
   javax.management.j2ee.statistics.JCAConnectionStats \
   javax.management.j2ee.statistics.JCAStats \
   javax.management.j2ee.statistics.JDBCConnectionPoolStats \
   javax.management.j2ee.statistics.JDBCConnectionStats \
   javax.management.j2ee.statistics.JDBCStats \
   javax.management.j2ee.statistics.JMSConnectionStats \
   javax.management.j2ee.statistics.JMSConsumerStats \
   javax.management.j2ee.statistics.JMSEndpointStats \
   javax.management.j2ee.statistics.JMSProducerStats \
   javax.management.j2ee.statistics.JMSSessionStats \
   javax.management.j2ee.statistics.JMSStats \
   javax.management.j2ee.statistics.JTAStats \
   javax.management.j2ee.statistics.JVMStats \
   javax.management.j2ee.statistics.JavaMailStats \
   javax.management.j2ee.statistics.MessageDrivenBeanStats \
   javax.management.j2ee.statistics.RangeStatistic \
   javax.management.j2ee.statistics.ServletStats \
   javax.management.j2ee.statistics.SessionBeanStats \
   javax.management.j2ee.statistics.StatefulSessionBeanStats \
   javax.management.j2ee.statistics.StatelessSessionBeanStats \
   javax.management.j2ee.statistics.Statistic \
   javax.management.j2ee.statistics.Stats \
   javax.management.j2ee.statistics.TimeStatistic \
   javax.management.j2ee.statistics.URLStats \
   javax.management.loading.ClassLoaderRepository \
   javax.management.loading.DefaultLoaderRepository \
   javax.management.loading.MLet \
   javax.management.loading.MLetMBean \
   javax.management.loading.PrivateClassLoader \
   javax.management.loading.PrivateMLet \
   javax.management.modelmbean.DescriptorSupport \
   javax.management.modelmbean.InvalidTargetObjectTypeException \
   javax.management.modelmbean.ModelMBean \
   javax.management.modelmbean.ModelMBeanAttributeInfo \
   javax.management.modelmbean.ModelMBeanConstructorInfo \
   javax.management.modelmbean.ModelMBeanInfo \
   javax.management.modelmbean.ModelMBeanInfoSupport \
   javax.management.modelmbean.ModelMBeanNotificationBroadcaster \
   javax.management.modelmbean.ModelMBeanNotificationInfo \
   javax.management.modelmbean.ModelMBeanOperationInfo \
   javax.management.modelmbean.RequiredModelMBean \
   javax.management.modelmbean.XMLParseException \
   javax.management.monitor.CounterMonitor \
   javax.management.monitor.CounterMonitorMBean \
   javax.management.monitor.GaugeMonitor \
   javax.management.monitor.GaugeMonitorMBean \
   javax.management.monitor.Monitor \
   javax.management.monitor.MonitorMBean \
   javax.management.monitor.MonitorNotification \
   javax.management.monitor.MonitorSettingException \
   javax.management.monitor.StringMonitor \
   javax.management.monitor.StringMonitorMBean \
   javax.management.openmbean.ArrayType \
   javax.management.openmbean.CompositeData \
   javax.management.openmbean.CompositeDataSupport \
   javax.management.openmbean.CompositeType \
   javax.management.openmbean.InvalidKeyException \
   javax.management.openmbean.InvalidOpenTypeException \
   javax.management.openmbean.KeyAlreadyExistsException \
   javax.management.openmbean.OpenDataException \
   javax.management.openmbean.OpenMBeanAttributeInfo \
   javax.management.openmbean.OpenMBeanAttributeInfoSupport \
   javax.management.openmbean.OpenMBeanConstructorInfo \
   javax.management.openmbean.OpenMBeanConstructorInfoSupport \
   javax.management.openmbean.OpenMBeanInfo \
   javax.management.openmbean.OpenMBeanInfoSupport \
   javax.management.openmbean.OpenMBeanOperationInfo \
   javax.management.openmbean.OpenMBeanOperationInfoSupport \
   javax.management.openmbean.OpenMBeanParameterInfo \
   javax.management.openmbean.OpenMBeanParameterInfoSupport \
   javax.management.openmbean.OpenType \
   javax.management.openmbean.SimpleType \
   javax.management.openmbean.TabularData \
   javax.management.openmbean.TabularDataSupport \
   javax.management.openmbean.TabularType \
   javax.management.relation.InvalidRelationIdException \
   javax.management.relation.InvalidRelationServiceException \
   javax.management.relation.InvalidRelationTypeException \
   javax.management.relation.InvalidRoleInfoException \
   javax.management.relation.InvalidRoleValueException \
   javax.management.relation.MBeanServerNotificationFilter \
   javax.management.relation.Relation \
   javax.management.relation.RelationException \
   javax.management.relation.RelationNotFoundException \
   javax.management.relation.RelationNotification \
   javax.management.relation.RelationService \
   javax.management.relation.RelationServiceMBean \
   javax.management.relation.RelationServiceNotRegisteredException \
   javax.management.relation.RelationSupport \
   javax.management.relation.RelationSupportMBean \
   javax.management.relation.RelationType \
   javax.management.relation.RelationTypeNotFoundException \
   javax.management.relation.RelationTypeSupport \
   javax.management.relation.Role \
   javax.management.relation.RoleInfo \
   javax.management.relation.RoleInfoNotFoundException \
   javax.management.relation.RoleList \
   javax.management.relation.RoleNotFoundException \
   javax.management.relation.RoleResult \
   javax.management.relation.RoleStatus \
   javax.management.relation.RoleUnresolved \
   javax.management.relation.RoleUnresolvedList \
   javax.management.timer.Timer \
   javax.management.timer.TimerMBean \
   javax.management.timer.TimerNotification \
   javax.resource.NotSupportedException \
   javax.resource.Referenceable \
   javax.resource.ResourceException \
   javax.resource.cci.Connection \
   javax.resource.cci.ConnectionFactory \
   javax.resource.cci.ConnectionMetaData \
   javax.resource.cci.ConnectionSpec \
   javax.resource.cci.IndexedRecord \
   javax.resource.cci.Interaction \
   javax.resource.cci.InteractionSpec \
   javax.resource.cci.LocalTransaction \
   javax.resource.cci.MappedRecord \
   javax.resource.cci.MessageListener \
   javax.resource.cci.Record \
   javax.resource.cci.RecordFactory \
   javax.resource.cci.ResourceAdapterMetaData \
   javax.resource.cci.ResourceWarning \
   javax.resource.cci.ResultSet \
   javax.resource.cci.ResultSetInfo \
   javax.resource.cci.Streamable \
   javax.resource.spi.ActivationSpec \
   javax.resource.spi.ApplicationServerInternalException \
   javax.resource.spi.BootstrapContext \
   javax.resource.spi.CommException \
   javax.resource.spi.ConnectionEvent \
   javax.resource.spi.ConnectionEventListener \
   javax.resource.spi.ConnectionManager \
   javax.resource.spi.ConnectionRequestInfo \
   javax.resource.spi.DissociatableManagedConnection \
   javax.resource.spi.EISSystemException \
   javax.resource.spi.IllegalStateException \
   javax.resource.spi.InvalidPropertyException \
   javax.resource.spi.LazyAssociatableConnectionManager \
   javax.resource.spi.LazyEnlistableConnectionManager \
   javax.resource.spi.LazyEnlistableManagedConnection \
   javax.resource.spi.LocalTransaction \
   javax.resource.spi.LocalTransactionException \
   javax.resource.spi.ManagedConnection \
   javax.resource.spi.ManagedConnectionFactory \
   javax.resource.spi.ManagedConnectionMetaData \
   javax.resource.spi.ResourceAdapter \
   javax.resource.spi.ResourceAdapterAssociation \
   javax.resource.spi.ResourceAdapterInternalException \
   javax.resource.spi.ResourceAllocationException \
   javax.resource.spi.SecurityException \
   javax.resource.spi.SharingViolationException \
   javax.resource.spi.UnavailableException \
   javax.resource.spi.ValidatingManagedConnectionFactory \
   javax.resource.spi.XATerminator \
   javax.resource.spi.endpoint.MessageEndpoint \
   javax.resource.spi.endpoint.MessageEndpointFactory \
   javax.resource.spi.security.GenericCredential \
   javax.resource.spi.security.PasswordCredential \
   javax.resource.spi.work.ExecutionContext \
   javax.resource.spi.work.Work \
   javax.resource.spi.work.WorkAdapter \
   javax.resource.spi.work.WorkCompletedException \
   javax.resource.spi.work.WorkEvent \
   javax.resource.spi.work.WorkException \
   javax.resource.spi.work.WorkListener \
   javax.resource.spi.work.WorkManager \
   javax.resource.spi.work.WorkRejectedException \
   javax.transaction.HeuristicCommitException \
   javax.transaction.HeuristicMixedException \
   javax.transaction.HeuristicRollbackException \
   javax.transaction.InvalidTransactionException \
   javax.transaction.NotSupportedException \
   javax.transaction.RollbackException \
   javax.transaction.Status \
   javax.transaction.Synchronization \
   javax.transaction.SystemException \
   javax.transaction.Transaction \
   javax.transaction.TransactionManager \
   javax.transaction.TransactionRequiredException \
   javax.transaction.TransactionRolledbackException \
   javax.transaction.UserTransaction \
   javax.transaction.xa.XAException \
   javax.transaction.xa.XAResource \
   javax.transaction.xa.Xid \
   javax.xml.namespace.QName \
   javax.xml.rpc.Call \
   javax.xml.rpc.JAXRPCException \
   javax.xml.rpc.NamespaceConstants \
   javax.xml.rpc.ParameterMode \
   javax.xml.rpc.Service \
   javax.xml.rpc.ServiceException \
   javax.xml.rpc.ServiceFactory \
   javax.xml.rpc.Stub \
   javax.xml.rpc.encoding.DeserializationContext \
   javax.xml.rpc.encoding.Deserializer \
   javax.xml.rpc.encoding.DeserializerFactory \
   javax.xml.rpc.encoding.SerializationContext \
   javax.xml.rpc.encoding.Serializer \
   javax.xml.rpc.encoding.SerializerFactory \
   javax.xml.rpc.encoding.TypeMapping \
   javax.xml.rpc.encoding.TypeMappingRegistry \
   javax.xml.rpc.encoding.XMLType \
   javax.xml.rpc.handler.GenericHandler \
   javax.xml.rpc.handler.Handler \
   javax.xml.rpc.handler.HandlerChain \
   javax.xml.rpc.handler.HandlerInfo \
   javax.xml.rpc.handler.HandlerRegistry \
   javax.xml.rpc.handler.MessageContext \
   javax.xml.rpc.handler.soap.SOAPMessageContext \
   javax.xml.rpc.holders.BigDecimalHolder \
   javax.xml.rpc.holders.BigIntegerHolder \
   javax.xml.rpc.holders.BooleanHolder \
   javax.xml.rpc.holders.BooleanWrapperHolder \
   javax.xml.rpc.holders.ByteArrayHolder \
   javax.xml.rpc.holders.ByteHolder \
   javax.xml.rpc.holders.ByteWrapperHolder \
   javax.xml.rpc.holders.CalendarHolder \
   javax.xml.rpc.holders.DoubleHolder \
   javax.xml.rpc.holders.DoubleWrapperHolder \
   javax.xml.rpc.holders.FloatHolder \
   javax.xml.rpc.holders.FloatWrapperHolder \
   javax.xml.rpc.holders.Holder \
   javax.xml.rpc.holders.IntHolder \
   javax.xml.rpc.holders.IntegerWrapperHolder \
   javax.xml.rpc.holders.LongHolder \
   javax.xml.rpc.holders.LongWrapperHolder \
   javax.xml.rpc.holders.ObjectHolder \
   javax.xml.rpc.holders.QNameHolder \
   javax.xml.rpc.holders.ShortHolder \
   javax.xml.rpc.holders.ShortWrapperHolder \
   javax.xml.rpc.holders.StringHolder \
   javax.xml.rpc.server.ServiceLifecycle \
   javax.xml.rpc.server.ServletEndpointContext \
   javax.xml.rpc.soap.SOAPFaultException \
   > "$mode.out"

echo "created: $mode.out"

# Not checked
# javax.security.jacc.EJBMethodPermission \
# javax.security.jacc.EJBRoleRefPermission \
# javax.security.jacc.HttpMethodSpec \
# javax.security.jacc.PolicyConfiguration \
# javax.security.jacc.PolicyConfigurationFactory \
# javax.security.jacc.PolicyContext \
# javax.security.jacc.PolicyContextException \
# javax.security.jacc.PolicyContextHandler \
# javax.security.jacc.URLPattern \
# javax.security.jacc.URLPatternSpec \
# javax.security.jacc.WebResourcePermission \
# javax.security.jacc.WebRoleRefPermission \
# javax.security.jacc.WebUserDataPermission \
