/*
 * RESTHeart - the data REST API server
 * Copyright (C) SoftInstigate Srl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.restheart.test.performance;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.restheart.Configuration;
import org.restheart.ConfigurationException;
import org.restheart.db.MongoDBClientSingleton;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Andrea Di Cesare <andrea@softinstigate.com>
 */
public abstract class AbstractPT {
    protected String url;

    protected String mongoHost;
    protected Integer mongoPort;
    protected String mongoAuthDb;
    protected String mongoUser;
    protected String mongoPassword;

    protected String id;
    protected String pwd;
    protected String db;
    protected String coll;
    
    protected Executor httpExecutor;
    
    public void prepare() {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(id, pwd.toCharArray());
            }
        });

        Configuration conf;

        StringBuilder ymlSB = new StringBuilder();

        if (mongoPort == null) {
            mongoPort = 27017;
        }

        if (mongoHost == null) {
            mongoHost = "127.0.0.1";
        }

        if (mongoHost != null) {
            ymlSB.append(Configuration.MONGO_SERVERS_KEY).append(":").append("\n");
            ymlSB.append("    - ").append(Configuration.MONGO_HOST_KEY).append(": ").append(mongoHost).append("\n");
            ymlSB.append("      ").append(Configuration.MONGO_PORT_KEY).append(": ").append(mongoPort).append("\n");
        }

        if (mongoAuthDb != null && mongoUser != null && mongoPassword != null) {
            ymlSB.append(Configuration.MONGO_CREDENTIALS_KEY).append(":").append("\n");
            ymlSB.append("    - ").append(Configuration.MONGO_AUTH_DB_KEY).append(": ").append(mongoAuthDb).append("\n");
            ymlSB.append("      ").append(Configuration.MONGO_USER_KEY).append(": ").append(mongoUser).append("\n");
            ymlSB.append("      ").append(Configuration.MONGO_PASSWORD_KEY).append(": ").append(mongoPassword).append("\n");
        }

        Yaml yaml = new Yaml();

        Map<String, Object> configuration = (Map<String, Object>) yaml.load(ymlSB.toString());

        try {
            MongoDBClientSingleton.init(new Configuration(configuration, true));
        } catch (ConfigurationException ex) {
            System.out.println(ex.getMessage() + ", exiting...");
            System.exit(-1);
        }

        httpExecutor = Executor.newInstance();

        // for perf test better to disable the restheart security
        if (url != null && id != null && pwd != null) {
            String host = "127.0.0.1";
            int port = 8080;
            String scheme = "http";

            try {
                URI uri = new URI(url);

                host = uri.getHost();
                port = uri.getPort();
                scheme = uri.getScheme();
            } catch (URISyntaxException ex) {
                Logger.getLogger(LoadPutPT.class.getName()).log(Level.SEVERE, null, ex);
            }

            httpExecutor.authPreemptive(new HttpHost(host, port, scheme)).auth(new HttpHost(host), id, pwd);
        }
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @param mongoHost the mongoHost to set
     */
    public void setMongoHost(String mongoHost) {
        this.mongoHost = mongoHost;
    }

    /**
     * @param mongoPort the mongoPort to set
     */
    public void setMongoPort(Integer mongoPort) {
        this.mongoPort = mongoPort;
    }

    /**
     * @param mongoAuthDb the mongoAuthDb to set
     */
    public void setMongoAuthDb(String mongoAuthDb) {
        this.mongoAuthDb = mongoAuthDb;
    }

    /**
     * @param mongoUser the mongoUser to set
     */
    public void setMongoUser(String mongoUser) {
        this.mongoUser = mongoUser;
    }

    /**
     * @param mongoPassword the mongoPassword to set
     */
    public void setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    /**
     * @param db the db to set
     */
    public void setDb(String db) {
        this.db = db;
    }

    /**
     * @param coll the coll to set
     */
    public void setColl(String coll) {
        this.coll = coll;
    }
    
}
