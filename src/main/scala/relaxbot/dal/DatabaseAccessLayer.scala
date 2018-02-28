package relaxbot.dal

import com.typesafe.scalalogging.StrictLogging
import relaxbot.models.{RelaxEvent, RelaxEventDBRepresentation}
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DatabaseAccessLayer extends RelaxEventDBRepresentation with DBDriver with StrictLogging{

  val driver: JdbcProfile
  import driver.api._

  protected val db: Database

  val allTables = Seq(relaxEventsTableQuery)

  def insert(event: RelaxEvent): Future[RelaxEvent] = {
    db.run(insertIntoDb(event))
  }

  def relaxEvents: Future[Seq[RelaxEvent]] = {
    db.run(relaxEventsTableQuery.result)
  }

  def createSchemaIfNotExists: Future[Seq[Unit]] = {
    val existingTablesFuture = db.run(MTable.getTables)
    existingTablesFuture.flatMap { existing =>
      val existingNames = existing.map(t => t.name.name)
      val tablesToCreate = allTables.filterNot(t => existingNames.contains(t.baseTableRow.tableName))
      val created = tablesToCreate.map(_.schema.create)
      db.run(DBIO.sequence(created))
    }
  }

  def closeDB(): Unit = {
    db.close()
  }


}
