/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;

import  java.util.*;
import  junit.framework.*;


public class TestQueryStem extends TestCase {

  private GrouperSession  s;
  private GrouperQuery    q;

  public TestQueryStem(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
    s = Constants.createSession();
    Constants.createGroups(s);
    q = new GrouperQuery(s);
  }

  protected void tearDown () {
    s.stop();
  }


  /*
   * TESTS
   */

  public void testQueryInvalidStemNoMembers() {
    Assert.assertNotNull(q);
    Assert.assertFalse(
      "stem: nothing", q.stem("this:namespace:does:not:exist")
    );
    Assert.assertTrue(
      "stem: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "stem: groups=0", q.getGroups().size() == 0
    );
    Assert.assertTrue(
      "stem: listValues=0", q.getListValues().size() == 0
    );
    Assert.assertTrue(
      "stem: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryValidStemNoMembers() {
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "stem: something", q.stem("root")
    );
    // This catches _root_ as well
    Assert.assertTrue(
      "stem: stems=3", q.getStems().size() == 3
    );
    Assert.assertTrue(
      "stem: groups=0", q.getGroups().size() == 0
    );
    // Admins && Stemmers
    Assert.assertTrue(
      "stem: listValues=3", q.getListValues().size() == 3
    );
    Assert.assertTrue(
      "stem: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryInvalidStemMembers() {
    Constants.addMembers(s);
    Assert.assertNotNull(q);
    Assert.assertFalse(
      "stem: nothing", q.stem("this:namespace:does:not:exist")
    );
    Assert.assertTrue(
      "stem: stems=0", q.getStems().size() == 0
    );
    Assert.assertTrue(
      "stem: groups=0", q.getGroups().size() == 0
    );
    Assert.assertTrue(
      "stem: listValues=0", q.getListValues().size() == 0
    );
    Assert.assertTrue(
      "stem: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryValidStemMembers() {
    Constants.addMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "stem: something", q.stem("root")
    );
    // This catches _root_ as well
    Assert.assertTrue(
      "stem: stems=3", q.getStems().size() == 3
    );
    Assert.assertTrue(
      "stem: groups=0", q.getGroups().size() == 0
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "stem: listValues=3", q.getListValues().size() == 3
    );
    Assert.assertTrue(
      "stem: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryValidStemMembersFuzzy() {
    Constants.addMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "stem: something", q.stem("roo")
    );
    // This catches _root_ as well
    Assert.assertTrue(
      "stem: stems=3", q.getStems().size() == 3
    );
    Assert.assertTrue(
      "stem: groups=0", q.getGroups().size() == 0
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "stem: listValues=3", q.getListValues().size() == 3
    );
    Assert.assertTrue(
      "stem: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryValidStemMembersDeeper() {
    Constants.addMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "stem: something", q.stem("root:a stem")
    );
    Assert.assertTrue(
      "stem: stems=2", q.getStems().size() == 2
    );
    Assert.assertTrue(
      "stem: groups=0", q.getGroups().size() == 0
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "stem: listValues=2", q.getListValues().size() == 2
    );
    Assert.assertTrue(
      "stem: members=0", q.getMembers().size() == 0
    );
  }

  public void testQueryValidStemMembersEvenDeeper() {
    Constants.addMembers(s);
    Assert.assertNotNull(q);
    Assert.assertTrue(
      "stem: something", q.stem("root:a stem:another stem")
    );
    Assert.assertTrue(
      "stem: stems=1", q.getStems().size() == 1
    );
    Assert.assertTrue(
      "stem: groups=0", q.getGroups().size() == 0
    );
    // Admins && Stemmers && Members
    Assert.assertTrue(
      "stem: listValues=1", q.getListValues().size() == 1
    );
    Assert.assertTrue(
      "stem: members=0", q.getMembers().size() == 0
    );
  }

}
