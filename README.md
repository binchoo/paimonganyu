# PaimonGanyu

## 3rd party Genshin Impact application

PaimonGanyu is a java code base for my custom Genshin Impact applications.
- Asynchronous & Automatic business workflows are based on PaimonGanyu's java modules.
- Webflux webclients (most of which implement synchronous apis) and API adapters are defiend to fetch player data from Hoyoverse APIs.
- My KAKAOTALK chatbot [paimonganyu](https://github.com/binchoo/paimonganyu-doc) utilizes PaimonGanyu's Springboot controllers to handle the skill response and payload.

## Engineering Wiki
[Notion: PaimonGanyu 엔지니어링](https://hollow-leotard-0e1.notion.site/PaimonGanyu-81337fdfe052499f98a2a347f30afbcd)

## All workflows

[Currently defined workflows](https://github.com/binchoo/paimonganyu/issues/1#issuecomment-1087132930)

There are workflows that realize various use cases for Genshin Impact players by leveraging the AWS Serveless Application Model. (AWS SAM)

Use cases:

- Daily Check-In
  - New User Event Daily Check-In
  - Cron-based Automatic Daily Check-In
- Code Redeem
  - New User Event Code Redemption
  - New Redeem Code Event Code Redepmtion
- Hoyopass Fanout
  - New User Hoyopass SNS(AWS SNS) Publishing
- Hoyopass CRUD Operations to DynamoDB Tables

## All modules

https://github.com/binchoo/PaimonGanyu/issues/7

## Project configurations

In order to run Springboot controllers and JUnit test classes, three `properties` files **MAY** be configured.

### applications.properties (required)

`:application> src> main> resources> applications.properties`

**Example:**

```properties
amazon.ssm.hoyopass.publickeyname = HoyopassRsaPublicKey
amazon.ssm.hoyopass.privatekeyname = HoyopassRsaPrivateKey
listUserDailyCheck.maxCount = 4
```

- `amazon.ssm.hoyopass.publickeyname`(String)

  Set <u>your</u> AWS SSM Parameter name that is holding a RSA public key that encrypts user's hoyopass credentials.

- `amazon.ssm.hoyopass.privatekeyname` (String)

  Set <u>your</u> AWS SSM Parameter name that is holding the RSA private key that can decrypt the encryption done by the previous public key.

- `listUserDailyCheck.maxCount` (Non-negative integer)

  Configure how many items to be displayed when `DailyCheckController` is requested to show a user history of `UserDailyCheck`.

### amazon.properties (optional)

`:application> src> test> resources> amazon.properties`

**Example:**

```properties
amazon.aws.accesskey=ASDFASDFASDFASDFASDF
amazon.aws.secretkey=asdfasfdfASDFASDFasdfasdfASDFASFd+-*/asdf
amazon.region=ap-northeast-2

amazon.dynamodb.endpoint=https://dynamodb.ap-northeast-2.amazonaws.com
#amazon.dynamodb.endpoint=http://localhost:3306

amazon.ssm.hoyopass.publickeyname = HoyopassRsaPublicKey
amazon.ssm.hoyopass.privatekeyname = HoyopassRsaPrivateKey
```

These properties are only required by local intergration test runs. 

The production environment is AWS Lambda, hence IAM roles and IAM policies are responsible to configure the same properties.

### accounts.properties (optional)

`:application> src> test> resources> accounts.properties`

Some JUnit tests require real Genshin Impact accounts to validate their functionalities. Hence, when any user authentication is not provided, those tests fail.

That being said, I know test account preparation is not an easy step. The `accounts.properties` is not required. You can give up running those test cases.

## Deployment steps

Rewriting...

### buildZip and copyBuiltZip tasks

`paimonganyu/PaimonGanyu/build.gradle`

Rewrinting...

#### buildZip

In order to deploy a compiled java lambda handler onto AWS lambda service, class itself and all other dependent runtime classpaths should be in one .jar or .zip file.
The `CodeUri` property of lambda resources should point to that file. Using maven with the shade plugin, we may just point to the root project folder, then SAM will transparently find the very right artifact to deploy.
See also, [Deploy Java Lambda functions with .zip or JAR file archives (AWS Docs)](https://docs.aws.amazon.com/lambda/latest/dg/java-package.html#java-package-libraries). 

#### copyBuiltZip

`CodeUri` property of lambdas in `template.yaml` means the place where the relating fat-zip or fat-jar is located. That path is relative to `.aws-sam/build` when not pointing to the root project folder.
Task `copyBuiltZip` moves the artifact which `buildZip` generated into the `.aws-sam/build/` directory. This is to shorten the path string for the `CodeUri`.

### Running a local system test
Some functionalities need to be verified with real-running infrastructures. eg. a service layer depending on a DynamoDB table.

A docker container can be created from `amazon/dynamodb-local` image, 
and system test classes can use its endpoint url (`http://localhost:3306`) to interact with dynamodb tables.

Running the container and including/excluding test classes for system test runs
are managed by the build script of `:application`.

To start a local system test, provide an argument named `-PlocalTest` and set this to be true.
This will run a dynamodb container before the test runs.
```bash
./gradlew -PlocalTest=true :application:test
---
> Task :application:stopRunningDynamoDBContainer
Container stopped: c02efd80497c

> Task :application:startDynamoDBContainer
Container started: fdd3f9b691ef9f026935ef2429d7d067c037b80fe4559225f58fbe12ae6b0394
```

## Help Needed for Quality Assurance

I hope redundant system analysis and test (A&T) activities go with this project.

I always welcome collaborators who can join A&T activities like below:

- Spec Review
- Code Review
- Bug Report/Bug Fix
- Proposal Kick-Off

## LICENSES

**GPLv3**

- [PaimonGanyu](https://github.com/binchoo/PaimonGanyu/blob/master/LICENSE)
- [PaimonGanyu:application](https://github.com/binchoo/PaimonGanyu/blob/master/PaimonGanyu/application/LICENSE)
- [PaimonGanyu:application:infra](https://github.com/binchoo/PaimonGanyu/blob/master/PaimonGanyu/application/LICENSE)

**MIT**

- [PaimonGanyu:awsutils](https://github.com/binchoo/PaimonGanyu/blob/master/PaimonGanyu/awsutils/LICENSE)
- [PaimonGanyu:hoyoapi](https://github.com/binchoo/PaimonGanyu/blob/master/PaimonGanyu/hoyoapi/LICENSE)
- [PaimonGanyu:ikakao](https://github.com/binchoo/PaimonGanyu/blob/master/PaimonGanyu/ikakao/LICENSE)
- [PaimonGanyu:domain](https://github.com/binchoo/PaimonGanyu/blob/master/PaimonGanyu/domain/LICENSE)
