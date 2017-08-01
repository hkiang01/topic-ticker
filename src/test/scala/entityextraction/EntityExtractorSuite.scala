package entityextraction

import edu.illinois.harrisonkiang.entityextraction.EntityExtractor
import org.scalatest.{FunSpec, Matchers}

class EntityExtractorSuite extends FunSpec with Matchers with EntityExtractor {

  describe("test") {
    val entities = extractEntities(
      "John Smith went to China. He visited Beijing, on January 10th, 2013." +
      " He later went to Paris, France.")

    it("entity extraction should work as expected") {
      entities should be (Array(
        ("John Smith", "PERSON"),
        ("China", "LOCATION"),
        ("Beijing", "LOCATION"),
        ("January 10th , 2013", "DATE"),
        ("Paris France", "LOCATION")
      ))
    }

  }
}
