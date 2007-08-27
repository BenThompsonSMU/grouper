/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.privs;
import  edu.internet2.middleware.grouper.Privilege;
import  edu.internet2.middleware.grouper.Stem;
import  edu.internet2.middleware.grouper.SubjectFinder;
import  edu.internet2.middleware.grouper.UnableToPerformException;
import  edu.internet2.middleware.subject.Subject;
import  java.util.Set;


/**
 * Decorator that provides <i>GrouperAll</i> privilege resolution for {@link NamingResolver}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GrouperAllNamingResolver.java,v 1.2 2007-08-27 15:46:24 blair Exp $
 * @since   @HEAD@
 */
public class GrouperAllNamingResolver extends NamingResolverDecorator {
  // TODO 20070820 DRY w/ access resolution

  
  private Subject all;



  /**
   * @since   @HEAD@
   */
  public GrouperAllNamingResolver(NamingResolver resolver) {
    super(resolver);
    this.all = SubjectFinder.findAllSubject();
  }



  /**
   * @see     NamingResolver#getConfig(String)
   * @throws  IllegalStateException if any parameter is null.
   */
  public String getConfig(String key) 
    throws IllegalStateException
  {
    return super.getDecoratedResolver().getConfig(key);
  }

  /**
   * @see     NamingResolver#getStemsWhereSubjectHasPrivilege(Subject, Privilege)
   * @since   @HEAD@
   */
  public Set<Stem> getStemsWhereSubjectHasPrivilege(Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    Set<Stem> stems = super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(subject, privilege);
    stems.addAll( super.getDecoratedResolver().getStemsWhereSubjectHasPrivilege(this.all, privilege) );
    return stems;
  }

  /**
   * @see     NamingResolver#getPrivileges(Stem, Subject)
   * @since   @HEAD@
   */
  public Set<Privilege> getPrivileges(Stem stem, Subject subject)
    throws  IllegalArgumentException
  {
    // TODO 20070820 include GrouperAll privs?
    return super.getDecoratedResolver().getPrivileges(stem, subject);
  }

  /**
   * @see     NamingResolver#getSubjectsWithPrivilege(Stem, Privilege)
   * @since   @HEAD@
   */
  public Set<Subject> getSubjectsWithPrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException
  {
    return super.getDecoratedResolver().getSubjectsWithPrivilege(stem, privilege);
  }

  /**
   * @see     NamingResolver#grantPrivilege(Stem, Subject, Privilege)
   * @since   @HEAD@
   */
  public void grantPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().grantPrivilege(stem, subject, privilege);
  }

  /**
   * @see     NamingResolver#hasPrivilege(Stem, Subject, Privilege)
   * @since   @HEAD@
   */
  public boolean hasPrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException
  {
    if ( super.getDecoratedResolver().hasPrivilege(stem, this.all, privilege) ) {
      return true;
    }
    return super.getDecoratedResolver().hasPrivilege(stem, subject, privilege);
  }

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Stem stem, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(stem, privilege);
  }
            

  /**
   * @see     NamingResolver#revokePrivilege(Stem, Subject, Privilege)
   * @since   @HEAD@
   */
  public void revokePrivilege(Stem stem, Subject subject, Privilege privilege)
    throws  IllegalArgumentException,
            UnableToPerformException
  {
    super.getDecoratedResolver().revokePrivilege(stem, subject, privilege);
  }            

}

