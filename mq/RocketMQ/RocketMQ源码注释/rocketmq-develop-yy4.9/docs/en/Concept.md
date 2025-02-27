# Basic Concept                                

## 1 Message Model

RocketMQ message model is mainly composed of Producer, Broker and Consumer. The producer is responsible for producing messages and the consumer is for consuming messages, while the broker stores messages.     
The broker is an independent server during actual deployment, and each broker can store messages from multiple topics. Even messages from the same topic can be stored in the different brokers by sharding strategy.     
The message queue is used to store physical offsets of messages, and the message addresses are stored in seperate queues. The consumer group consists of multiple consumer instances.    
##  2 Producer    
The Producer is responsible for producing messages, typically by business systems. It sends messages generated by the systems to brokers. RocketMQ provides multiple paradigms of sending: synchronous, asynchronous, sequential and one-way. Both synchronous and asynchronous methods require the confirmation information return from the Broker, but one-way method does not require it.
## 3 Consumer
The Consumer is responsible for consuming messages, typically the background system is responsible for asynchronous consumption. The consumer pulls messages from brokers and feeds them into application. From the perspective of user, two types of consumers are provided: pull consumer and push consumer.
## 4 Topic
The Topic refers to a collection of one kind of message. Each topic contains several messages and one message can only belong to one topic. The topic is the basic unit of RocketMQ for message subscription.
## 5 Broker Server
As the role of the transfer station, the Broker Server stores and forwards messages. In RocketMQ, the broker server is responsible for receiving messages sent from producers, storing them and preparing to handle pull requests. It also stores the related message meta data, including consumer groups, consuming progress, topics, queues info and so on.
## 6 Name Server
The Name Server serves as the provider of routing service. The producer or the consumer can find the list of broker IP addresses for each topic through name server. Multiple name servers can be deployed in one cluster, but they are independent of each other and do not exchange information.
## 7 Pull Consumer
A type of Consumer, the application pulls messages from brokers by actively invoking the consumer pull message method, and the application has the advantages of controlling the timing and frequency of pulling messages. Once the batch of messages is pulled, user application will initiate consuming process.                    
## 8 Push Consumer
A type of Consumer, Under this high real-time performance mode, it will push the message to the consumer actively when the Broker receives the data.                       
## 9 Producer Group
A collection of the same type of Producer, which sends the same type of messages with consistent logic. If a transaction message is sent and the original producer crashes after sending, the broker server will contact other producers in the same producer group to commit or rollback the transactional message.
## 10 Consumer Group
A collection of the same type of Consumer, which consume the same type of messages with consistent logic. The consumer group makes load-balance and fault-tolerance super easy in terms of message consuming.
Warning: consumer instances of one consumer group must have exactly the same topic subscription(s).   

RocketMQ supports two types of consumption mode:Clustering and Broadcasting.
## 11 Consumption Mode - Clustering
Under the Clustering mode, all the messages from one topic will be delivered to all the consumers instances averagely as much as possible. That is, one message can be consumed by only one consumer instance.
## 12 Consumption Mode - Broadcasting
Under the Broadcasting mode, each consumer instance of the same consumer group receives every message published to the corresponding topic.
## 13 Normal Ordered Message
Under the Normal Ordered Message mode, the messages received by consumers from the same ConsumeQueue are sequential, but the messages received from the different message queues may be non-sequential.
## 14 Strictly Ordered Message
Under the Strictly Ordered Message mode, all messages received by the consumers from the same topic are sequential as the order they are stored.
## 15 Message
The physical carrier of information transmitted by a messaging system, the smallest unit of production and consumption data, each message must belong to one topic.
Each Message in RocketMQ has a unique message id and can carry a key used to store business-related value. The system has the function to query messages by its id or key.
## 16 Tag
Flags set for messages to distinguish different types of messages under the same topic, functioning as a "sub-topic". Messages from the same business unit can set different tags under the same topic in terms of different business purposes. The tag can effectively maintain the clarity and consistency of the code and optimize the query system provided by RocketMQ. The consumer can realize different "sub-topic" by using tag in order to achieve better expansibility.
