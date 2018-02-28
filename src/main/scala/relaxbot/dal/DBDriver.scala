package relaxbot.dal

import slick.jdbc.JdbcProfile


trait DBDriver {
  protected val driver: JdbcProfile
}
