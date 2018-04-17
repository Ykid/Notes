object MongoDBCheatSheet extends App {

  /**
    * Decision Tree
    * # are you talking to a single node or a cluster ?
    * >>> http://mongodb.github.io/mongo-scala-driver/2.2/getting-started/quick-tour/#make-a-connection
    * # what is your write concern ?
    * >>> http://mongodb.github.io/mongo-scala-driver/2.2/reference/crud/
    * # what is your read preference ?
    * >>> http://mongodb.github.io/mongo-scala-driver/2.2/reference/crud/
    *
    * Collection Subtype
    * Immutable Documents
    * >>> org.mongodb.scala.Document
    * Mutable Documents
    * >>> org.mongodb.scala.collections.mutable.Document
    *
    * BsonDocument supports following types
    * BsonValue is the type safe representation of a Bson type from the org.bson library, it represents specific value types.
    *
    * BSON type	Scala type correspondence
    * Document	org.mongodb.scala.bson.Document
    * Array	List
    * Date	Date or int (ms since epoch)
    * Boolean	Boolean
    * Double	Double
    * Int32	Integer
    * Int64	Long
    * String	String
    * Binary	Array[Byte]
    * ObjectId	ObjectId
    * Null	None
    **/

  //####################
  // serialization
  //####################
  /**
    *
    * # is your data written in CASE CLASS ?
    * >>> if those classes are of simple types(Nested Case classes are okay, case classes extending sealed traits are okay too). use Macro.
    * >>> The order of arguments passed into `org.bson.codecs.configuration.CodecRegistries.fromRegistries` does not matter
    * >>> if Macro does not work, create your own codec
    * Regarding develop your own codec.
    * if your data needs codec of other class, please create a `CodecProvider`
    * if your data does not need other codecs, please extends a `Codec``
    * ref: http://mongodb.github.io/mongo-scala-driver/2.2/bson/macros/
    **/

  import org.mongodb.scala.bson.codecs.Macros
  import org.bson.codecs.configuration.CodecRegistries._
  import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY

  val personCodecProvider = Macros.createCodecProvider[MyData]()
  val codecRegistry = fromRegistries(fromProviders(personCodecProvider), DEFAULT_CODEC_REGISTRY)

  case class MyData(foo: String)

  //####################
  // standard template
  //#################### 
  import org.mongodb.scala._
  import scala.collection.JavaConverters._
  import org.mongodb.scala.connection.ClusterSettings
  //==============choose one amount these=======================
  // It is recommended to share one MongoClient since it represents a pool of collection
  // To directly connect to the default server localhost on port 27017
  val mongoClient1: MongoClient = MongoClient()

  // Use a Connection String
  val mongoClient2: MongoClient = MongoClient("mongodb://localhost")

  // or provide custom MongoClientSettings
  val clusterSettings: ClusterSettings = ClusterSettings.builder().hosts(List(new ServerAddress("localhost")).asJava).build()
  val settings: MongoClientSettings = MongoClientSettings.builder().clusterSettings(clusterSettings).build()
  val mongoClient3: MongoClient = MongoClient(settings)
  //=====================================


  val database: MongoDatabase = mongoClient1.getDatabase("mydb")

  //remember to clean up
  mongoClient3.close()
  
  //####################

  //####################
  // Query related
  //####################
  //to create queries
  import org.mongodb.scala.model.{Filters, Projections, Sorts, Updates}

  Filters.equal("", "")
  Projections.elemMatch("some field")
  Sorts.ascending("somefield")
  Updates.set("field", "new value")

  //if you don't want to find the suited method in the jungle
  import org.mongodb.scala.bson.collection.mutable.Document
  Document(json = "json string")

  //to use your codec yourself
  import org.bson.BsonDocumentWrapper
  BsonDocumentWrapper
    .asBsonDocument(MyData, codecRegistry)

  /**
    * pitfall
    * - convert findOne to Future[Option]
    **/

  database.getCollection[MyData]("name").find().first().toFuture().map(x => Option(x))
}
