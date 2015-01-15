package net.moznion.javadocio.badges;

import lombok.Data;
import lombok.EqualsAndHashCode;

import me.geso.tinyorm.Row;
import me.geso.tinyorm.annotations.Table;

@Table("badge")
@Data
@EqualsAndHashCode(callSuper = false)
public class Badge extends Row<Badge> {
  private String group_id;
  private String artifact_id;
  private String version;
  private String svg;
  private long last_accessed_at;
}
