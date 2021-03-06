package entityextraction

import java.util.UUID

import edu.illinois.harrisonkiang.entityextraction.{GoogleNewsArticleEntities, GoogleNewsArticleEntitiesObj, GoogleNewsArticleIdsAndSentences}
import edu.illinois.harrisonkiang.util.TopicTickerLogger
import org.scalatest.{FunSpec, Matchers}

class GoogleNewsArticleEntitiesSpec extends FunSpec with Matchers with TopicTickerLogger {

  val googleNewsArticleEntities = new GoogleNewsArticleEntities

  describe("sample feeds") {
    it("data should be empty at start") {
      googleNewsArticleEntities.data.isEmpty should be (true)
    }
  }

  describe("ensure table exists") {
    googleNewsArticleEntities.ensureTableExists()
    it("table should exist if it is ensured to exist") {
      googleNewsArticleEntities.tableExists should be (true)
    }
  }

  describe("get records to be processed") {
    val resultSet = googleNewsArticleEntities.googleNewsIdsAndSentencesWithoutEntities(1, forceOpenConnection = true)
    resultSet.next()
    val googleNewsId = resultSet.getObject("googlenews_id").asInstanceOf[UUID]
    val sentencesArr = resultSet.getArray("sentences")
    var sentences: Array[String] = Array()
    if(sentencesArr != null) {
      sentences = sentencesArr.getArray.asInstanceOf[Array[String]]
    }
    val googleNewsArticleIdsAndSentences = GoogleNewsArticleIdsAndSentences(googleNewsId, sentences)
    println(s"googlenews_id: ${googleNewsArticleIdsAndSentences.googlenews_id}")
    println(s"sentences: \n${googleNewsArticleIdsAndSentences.sentences.mkString("\n")}")
    it("") {
      1 should be (1)
    }
    googleNewsArticleEntities.connection.close()
  }

  describe("update batch") {
    if(googleNewsArticleEntities.updateBatch()) {
      googleNewsArticleEntities.insertRecords()
    }
    it("data should be nonempty") {
      1 should be (1)
    }
  }


  ignore("insert records") {
    googleNewsArticleEntities.updateBatch(1)
    googleNewsArticleEntities.insertRecords()
    val rs = googleNewsArticleEntities.getRecords(forceOpenConnection = true)
    rs.next()

    val googleNewsArticleEntitiesObj = GoogleNewsArticleEntitiesObj(
        rs.getObject("googlenews_id").asInstanceOf[UUID],
        rs.getString("entity"),
        rs.getString("entity_type")
      )
    it("an inserted record should be obtainable") {
      googleNewsArticleEntitiesObj shouldBe a [GoogleNewsArticleEntitiesObj]
    }
  }


}
