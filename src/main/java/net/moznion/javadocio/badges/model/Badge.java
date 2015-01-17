package net.moznion.javadocio.badges.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Column;
import me.geso.tinyorm.annotations.PrimaryKey;
import me.geso.tinyorm.annotations.Table;
import me.geso.tinyorm.annotations.UpdatedTimestampColumn;

@Table("badge")
@Data
@EqualsAndHashCode(callSuper = false)
public class Badge extends Row<Badge> {
  @PrimaryKey
  private String group_id;

  @PrimaryKey
  private String artifact_id;

  @PrimaryKey
  private String version;

  @Column
  private String svg;

  @UpdatedTimestampColumn
  private long last_accessed_at;
}
