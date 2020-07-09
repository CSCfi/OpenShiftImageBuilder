# How to deploy Openshift-Image-Builder application on Openshift


### 1. Create a service account and give rights
In order for the application to interact with Openshift REST APIs, you need to use an authentication token (instead of passing your own credentials). The service account is best suited for this case. 
The first step is to create a new project/namespace. **Preferably, different from the project where you are currently deploying this application!**
Second step is to create a service account and then giving it the *edit* rights

```
oc create sa <service_account_name>
oc policy add-role-to-user edit -z <serviceaccount_name>

```

Now you should do `oc describe sa <serviceaccount_name` which would you show you the *secret* object's name containing the token. 
You should **copy the token** from the secret object for the next step.

### 2. Fill in cluster.properties with correct values and create a Secret object
Locate the file *cluster.properties* in this directory, and fill in the correct values for the following variables:

- OS_ENDPOINT : The openshift cluster endpoint (example, https://rahti.csc.fi:8443/ )
- NAMESPACE: The Namespace/Project where the images would be built and stored, this is the namespace where your service account from Step 1 also exists. **Preferably, different from the project where you are currently deploying this application!**
- TOKEN: The token obtained from service account in Step 1

After you have filled in the values to *cluster.properties* file, you need to create a secret object out of it using the following command

```
oc create secret generic <application_name>-cluster-properties --from-file=cluster.properties
```

### 3. Create a Secret object out of the private key

Openshift-Image-Builder uses JWT(Json Web Tokens) based authentication to validate the tokens using the user provided private key, which can be generated using the standard openssl command `openssl genrsa -out <private_key_filename> 2048`. This private key needs to be converted into a secret for the application to use it.

Use the following command to convert it into a Secret object:

```
oc create secret generic test-privkey --from-file=<private_key_filename>

```

### 4. Process the template, provide the parameters and apply

The final step is to process the template provided in this directory, provide the user defined variables which are **APPLICATION_NAME: the name of your application** and **PEM_FILENAME: the name of the private key file from Step 3**

```
oc process -f template.yml -p APPLICATION_NAME=<application_name> -p PEM_FILENAME=<private_key_filename> | oc apply -f -