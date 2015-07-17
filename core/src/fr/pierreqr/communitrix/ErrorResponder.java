package fr.pierreqr.communitrix;

public interface ErrorResponder {
  public    enum    MessageType       { Debug, Success, Message, Warning, Error };
  public    void    showMessage       (final MessageType type, final String message);
}
