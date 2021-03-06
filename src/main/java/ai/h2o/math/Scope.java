package ai.h2o.math;

import java.util.HashSet;
import java.util.Stack;
import water.H2O;
import water.Key;
import water.Lockable;

// A "scope" for tracking Key lifetimes.
//
// A Scope defines a *SINGLE THREADED* local lifetime management context,
// stored in Thread Local Storage.  Scopes can be explicitly entered or exited.
// User keys created by this thread are tracked, and deleted when the scope is
// exited.  Since enter & exit are explicit, failure to exit means the Keys
// leak (there is no reliable thread-on-exit cleanup action).  You must call
// Scope.exit() at some point.  Only user keys & Vec keys are tracked.
//
// Scopes support nesting.  Scopes support partial cleanup: you can list Keys
// you'd like to keep in the exit() call.  These will be "bumped up" to the
// higher nested scope - or escaped and become untracked at the top-level.

public class Scope {
  // Thread-based Key lifetime tracking
  static private final ThreadLocal<Scope> _scope = new ThreadLocal<Scope>() {
    @Override protected Scope initialValue() { return new Scope(); }
  };
  private final Stack<HashSet<Key>> _keys = new Stack<HashSet<Key>>();

  static public void enter() { _scope.get()._keys.push(new HashSet<Key>()); }
  static public void exit () {
    Stack<HashSet<Key>> keys = _scope.get()._keys;
    if( keys.size()==0 ) return;
    for( Key key : keys.pop() )
      Lockable.delete(key);
  }
  static public Key exit(Key key) { throw H2O.unimpl(); }
  static public Key[] exit(Key... key) { throw H2O.unimpl(); }

  static public void track( Key k ) {
    if( !k.user_allowed() && !k.isVec() ) return; // Not tracked
    Scope scope = _scope.get();                   // Pay the price of T.L.S. lookup
    if( scope == null ) return; // Not tracking this thread
    if( scope._keys.size() == 0 ) return; // Tracked in the past, but no scope now
    scope._keys.peek().add(k);            // Track key
  }
}
