# Overview

## Core:

 - `OSController` : De facto Main EntryPoint - Containing the REST Endpoints
 - `OCRestClient`: Responsible for interacting with OpenShift REST APIs

## Utilities:

 - `OSJsonParser`: Parses the 
	- JSON Objects shipped with this application in "Resources"
 	- JSON Strings received for the different kinds of Objects from the OpenShift REST APIs
 - `Utils`: Responsible for :
	- reading the custom configuration (if present) or the default configuration
	- Switching on the debug mode
	- Generating OpenShift REST API URLs based on the given parameters
	- Calculating the SHA1 Hash based on the given parameters

## Security:

 - `UserController`: Responsible for signup (creating new users)
 - `Authentication`: Logging In and generating a JWT Token
 - `Authorization`: Verifying the JWT Token
 - `SecurityConfig`: Configuration for securing the different endpoints of this application
 - `SecurityConstants`: Contains app security related constants like path of the public/private keys, token and namespace file paths 

## Database:
The database used is H2, operated through Hibernate ORM
 
 - `ApplicationUserRespository`: Used for ORM purposes, the corresponding table created in the database is ‘APPLICATION_USER’
 - `ApplicationUser`: The model used for the above table
 - `UserDetailsServiceImpl`: Implementation of Spring's UserDetailsService which is backed by the database to retrieve user details for authentication purposes

## Resources:

 - `application.properties`: Contains app specific important configuration - database configuration, h2 console (web based access to the database) and logging configuration
NOTE: This file is overridden using the OpenShift template for this application

 - JSON Files containing `BuildConfigs , BuildRequest, ImageStream` configuration templates which are templated with desired values and used during POST requests for creating BuildConfig, Build, or ImageStream
 There are two different types of BuildConfig templates:
 1. `BuildConfigSource`: When building from the source using s2i
 2. `BuildConfigDocker`: When building from a dockerfile

___

# In-Depth

## Application's REST Endpoints

All the BuildConfigs and ImageStreams are identified using a unique hash created out of the following parameters:
1. **Git URL (Mandatory)**
2. **Git Branch (Optional, Default: master)**
3. **Context Directory (Optional)**

#### Primary (Used in Frontend) -> 
1. **Create a BuildConfig with ImageStream (POST)**: BuildConfigs and ImageStreams go hand in hand, so we need to create both together, if one fails, the other must not be created!

2. **Creating/Starting a Build out of BuildConfig (POST)**: The builds are identified using their corresponding BuildConfig which in turn corresponds to the unique hash value described above.
Parameter required: BuildConfig name

3. **Getting ImageStream URL (GET)**: Gets the URL of the Image if it exists.
Parameter required: BuildConfig/ImageStream name

4. **Getting Build Status (GET)**: Used to track the progress of a specific Build. Status could be “Pending”, “Running”, “Completed” or “Failed”.
Parameter: BuildConfig name. When there are multiple Builds corresponding to a BuildConfig, fetch the status of the latest build !

5. **Getting Build Logs (GET)**: Used to periodically fetch logs of a specific Build while the build is running.
Parameter: Build name


#### Secondary ->

1. **Get All/Single BuildConfig(s) (GET)**
2. **Get All ImageStream URLs (GET)**
3. **Get a specific Build from the list of Builds of a BuildConfig (GET)**
4. **Delete BuildConfig (DELETE)**
5. **Delete Build (DELETE), Parameter: Build name**
6. **Delete all Builds (DELETE), Parameter: BuildConfig name**
7. **Delete ImageStream (DELETE)**


## OpenShift REST Client (For contacting the REST APIs of Openshift)

Each request receieved at the OSController is processed and delivered to the REST APIs of the OpenShift cluster. The process looks like this :

	Client -> OSController -> OCRestClient -> OpenShift Cluster

The OCRestClient uses Utils and OSJsonParser as helpers to carry out the following things:
1. `Utils` : Responsible for generating the correct REST API URL required for different OpenShift object 'kind' , which is used by the OCRestClient to send the request to the OpenShift's REST API
2. `OSJsonParser`: It performs two tasks:
	1. Generating the POST Body request by building the Json based request (using the JSON templates from "Resources")
	2. Parsing the Response received from the OpenShift REST APIs and extracting the relevant information to send back to the user
	
---

## Authentication:
By default Spring listens to all the login requests (with credentials) at /login
Spring's login mechanism is pretty good, so it would automatically check for valid credentials from the database once we configure the `SecurityConfig`'s authentication manager to use our `UserDetailsServiceImpl` (Implementation of Spring's UserDetailsService which is backed by the database to retrieve user details for authentication purposes)

Upon Successful authentication, the following things happen:

 * The private key in PKCS8 Format (as required by Java, provided by init container of the OpenShift template) is read as an RSA PrivateKey object.
 * A JWT Token is created using the username, desired expiration time and signed with our provided private key
 * This is returned in the Response Header as well as the Response Body

---

## Authorization:

In the first step, the header is checked, if the token is present. 

 *  The public Key in the DER Format (as required by Java, provided by the init container of the OpenShift template) is read as an RSA PublicKey Object.
 *  The received token is then parsed using the public key, after checking if a) the token is not malformed, b) token hasn’t expired c) the private key used for signing matches with the public key

If all the conditions are met, the authorization succeeds.

---

## Database:

The application uses H2 Database on a file based storage (path configured in application.properties via OpenShift template's ConfigMap) to store the user credentials.
Spring is easily able to configure the Database via Hibernate ORM using Spring's JPA feature
The credentials for accessing the database are also defined in application.properties

To access the database, we need to connect it using a web browser at the following url `http(s)://<base_url>/h2-console`, the 'h2-console' path is configurable in application.properties.

When logging in, make sure that the correct filepath of the database is displayed (again, the filepath is defined in application.properties)
The database file is mounted via a PVC (Persistent Volume Claim) in OpenShift template.
