package fr.pierreqr.communitrix.modelTemplaters;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public interface ModelTemplater {
  Model   build   (final ModelBuilder builder);
  void    dispose ();
}
