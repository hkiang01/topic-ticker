import org.clulab.processors.corenlp.CoreNLPProcessor
import org.clulab.processors.shallownlp.ShallowNLPProcessor
import org.clulab.processors.Processor

val proc:Processor = new CoreNLPProcessor(withDiscourse = ShallowNLPProcessor.WITH_DISCOURSE)

val doc = proc.annotate("John Smith went to China. He visited Beijing, on January 10th, 2013.")
for (sentence <- doc.sentences) {
  sentence.entities.foreach(entities => println(s"Named entities: ${entities.mkString(" ")}"))
}