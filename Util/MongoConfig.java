package configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@EnableMongoRepositories(basePackages = { "models.premiumAD", "repository.premiumAD" })
//
////Tell Spring to turn on WebMVC (e.g., it should enable the DispatcherServlet
////so that requests can be routed to our Controllers)
//
@Configuration
@PropertySource("classpath:${monetizationapienv}.mongodb.properties")
public class MongoConfig extends AbstractMongoConfiguration{
	
	
	@Autowired
	Environment env;

	@Override
	protected String getDatabaseName() {
		return env.getProperty("mongo.database");
		//return PREMIUM_AD_PRICE_DB;
	}

	@Override
	public Mongo mongo() throws Exception {
		Mongo mongo = new MongoClient( env.getProperty("mongo.host"),env.getProperty("mongo.port", Integer.class));
		play.Logger.info("Initialized mongo bean");
		return mongo;
	}
	

	
	
	@Bean
	public  MongoTemplate mongoTemplate() throws Exception {
	    return new MongoTemplate(mongo(), getDatabaseName());
	}

}
