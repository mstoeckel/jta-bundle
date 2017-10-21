# jta-bundle
A simple [Dropwizard](http://www.dropwizard.io) bundle for providing [JTA](http://www.oracle.com/technetwork/java/javaee/jta/index.html) transaction support.

The implementation is provided by [Narayana](http://narayana.io) Transaction Manager.

### Usage
Add the following to your `build.gradle`:
```
repositories {
	jcenter()
}

dependencies {
	compile "com.cognodyne.dw:jta-bundle:$bundleVersion"
}
```

### Examples
The following example includes the use of `jpa-bundle`; however, `jta-bundle` can be used independent of `jpa-bundle`.
```
@ApplicationScoped
public class ExampleServer extends Application<ExampleConfiguration> {
    @Inject
    private CdiBundle            cdiBundle;
    @Inject
    private JtaBundle            jtaBundle;
    @Inject
    private JpaBundle            jpaBundle;
        
	@Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(this.cdiBundle);
        bootstrap.addBundle(this.jtaBundle);
        bootstrap.addBundle(this.jpaBundle);
    }
    
	public static void main(String... args) {
        try {
            CdiBundle.application(ExampleServer.class, args)//
                    .with(ResourceInjectionServiceProvider.getInstance())// to support @Resource 
                    .with(TransactionServiceProvider.getInstance())// to add jta support
                    .with(JpaServiceProvider.getInstance())// Hibernate backed jpa support
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public interface UserService {
    @GET
    @Timed
    public List<User> getUsers();

    @GET
    @Path("/{id}")
    @Timed
    public User getUser(@PathParam("id") String id);

    @POST
    @Path("/{userId}")
    @Timed
    public void createUser(@PathParam("userId") String userId);

    @PUT
    @Path("/{id}/{userId}")
    @Timed
    public void updateUser(@PathParam("id") String id, @PathParam("userId") String userId);

    @DELETE
    @Path("/{id}")
    @Timed
    public void delete(@PathParam("id") String id);
}

public class UserResource implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserResource.class);
    @PersistenceContext(unitName = "exampleUnit")
    private EntityManager       em;

    @SuppressWarnings("unchecked")
    @Override
    @Transactional
    public List<User> getUsers() {
        return ((List<CdiUser>) em.createQuery("select u from CdiUser u")//
                .getResultList()).stream()//
                        .map(u -> new User(u.getId(), u.getUserId()))//
                        .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public User getUser(String id) {
        CdiUser user = em.find(CdiUser.class, id);
        return user == null ? null : new User(user.getId(), user.getUserId());
    }

    @Override
    @Transactional
    public void createUser(String userId) {
        CdiUser user = new CdiUser();
        user.setUserId(userId);
        em.persist(user);
    }

    @Override
    @Transactional
    public void updateUser(String id, String userId) {
        CdiUser user = em.find(CdiUser.class, id);
        if (user == null) {
            logger.warn("user:{} does not exist", id);
        } else {
            user.setUserId(userId);
            em.merge(user);
        }
    }

    @Override
    @Transactional
    public void delete(String id) {
        CdiUser user = em.find(CdiUser.class, id);
        if (user == null) {
            logger.warn("user:{} does not exist", id);
        } else {
            em.remove(user);
        }
    }
}
```