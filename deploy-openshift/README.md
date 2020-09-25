# How to deploy OpenshiftImageBuilder application on Openshift

There are two modes of deployments:
1. Default Mode: configures the application to use the current project for storing the Builds and Images
2. Custom Mode: configures the application to use another project for storing the Builds and Images

The deployment requires 4 key variables :

| Variable        | Default  Mode         | Custom Mode |
| ------------- |:-------------:| -----:|
| OpenShift Cluster URL (For contacting the REST APIs)     | Passed via environment variable | Passed via environment variable |
| OpenShift Image Registry URL (For storing the images)      | Passed via environment variable      |  Passed via environment variable |
| OpenShift Project (For Builds and ImageStreams)  |  Uses the current project (used for deploying this application)     |   Passed via environment variable |
| OpenShift Service Account Token (For authorizing the requests to the REST APIs) |  Created by the OpenShift template and used by default     |   Passed via environment variable |

## Using Default Configuration

In the default configuration mode, the Service Account is created by default in OpenShift template. The service account has a corresponding token which can be read at the following path `/var/run/secrets/kubernetes.io/serviceaccount/token`
Similarly, for the OpenShift project, it can be read at `/var/run/secrets/kubernetes.io/serviceaccount/namespace`

### 1. Create a Secret object out of the private key

OpenShiftImageBuilder uses JWT(Json Web Tokens) based authentication to validate the tokens using the user provided private key, which can be generated using the standard openssl command `openssl genrsa -out <private_key_filename> 2048`. This private key needs to be converted into a secret for the application to use it.

Use the following command to convert it into a Secret object:

```
oc create secret generic <application_name>-privkey --from-file=<private_key_filename>

```

### 2. Process the template, provide the parameters and apply

The final step is to process the template provided in this directory, provide the required  variables which are:
1. **APPLICATION_NAME: the name of your application** 
2. **PEM_FILENAME: the name of the private key file from Step 1**
3. **OPENSHIFT_CLUSTER_URL: URL of the OpenShift cluster to use**, *eg. https://rahti.csc.fi:8443*
4. **OPENSHIFT_IMAGE_REGISTRY_URL: URL of the OpenShift Image Registry to store the images (NO HTTP/HTTPS!)**, *eg. docker-registry.rahti.csc.fi*

```
oc process -f template.yml -p APPLICATION_NAME=<application_name> -p PEM_FILENAME=<private_key_filename>| oc apply -f -

```

---

## Using Custom Configuration

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

### 2. Create a Secret object out of the private key

Similar to the default configuration.

Use the following command to convert it into a Secret object:

```
oc create secret generic test-privkey --from-file=<private_key_filename>

```

### 3. Process the template, provide the parameters and apply

The final step is to process the template provided in this directory, provide the required  variables. 
**NOTE: Number of requires variables is more than the default configuration mode**

1. **APPLICATION_NAME: the name of your application** 
2. **PEM_FILENAME: the name of the private key file from Step 2**
3. **OPENSHIFT_CLUSTER_URL: URL of the OpenShift cluster to use**, *eg. https://rahti.csc.fi:8443*
4. **OPENSHIFT_IMAGE_REGISTRY_URL: URL of the OpenShift Image Registry to store the images (NO HTTP/HTTPS!)**, *eg. docker-registry.rahti.csc.fi*
5. **OPENSHIFT_SERVICE_ACCOUNT_TOKEN: Token copied from Step 1**
6. **OPENSHIFT_CUSTOM_PROJECT: Custom OpenShift project to use**

```
oc process -f template.yml -p APPLICATION_NAME=<application_name> -p PEM_FILENAME=<private_key_filename> -p OPENSHIFT_CLUSTER_URL=https://rahti.csc.fi:8443 -p OPENSHIFT_IMAGE_REGISTRY_URL=docker-registry.rahti.csc.fi -p OPENSHIFT_SERVICE_ACCOUNT_TOKEN=<token> -p OPENSHIFT_CUSTOM_PROJECT=<custom_project_name> | oc apply -f -

```
