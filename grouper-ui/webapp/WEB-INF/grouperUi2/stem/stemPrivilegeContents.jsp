<%@ include file="../assetsJsp/commonTaglib.jsp"%>

              <form class="form-inline form-small" name="stemPrivilegeFormName" id="stemPrivilegeFormId">
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                  <thead>
                    <tr>
                      <td colspan="7" class="table-toolbar gradient-background">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="people-update">${textContainer.text['stemPrivilegesUpdateBulkLabel'] }</label>
                          </div>
                          <div class="span5">
                            <select id="people-update" class="span12" name="stemPrivilegeBatchUpdateOperation">
                              <%-- create group should be the default, so list it first --%>
                              <option value="assign_creators">${textContainer.text['groupPrivilegesAssignCreatePrivilege'] }</option>
                              <option value="assign_stemAdmins">${textContainer.text['groupPrivilegesAssignStemAdminPrivilege'] }</option>
                              <option value="assign_stemAttrReaders">${textContainer.text['groupPrivilegesAssignStemAttributeReadPrivilege'] }</option>
                              <option value="assign_stemAttrUpdaters">${textContainer.text['groupPrivilegesAssignStemAttributeUpdatePrivilege'] }</option>
                              <option value="assign_stemViewers">${textContainer.text['groupPrivilegesAssignStemViewPrivilege'] }</option>
                              <option value="assign_all">${textContainer.text['groupPrivilegesAssignAllStemPrivilege'] }</option>
                              <option value="revoke_creators">${textContainer.text['groupPrivilegesRevokeCreatePrivilege'] }</option>
                              <option value="revoke_stemAdmins">${textContainer.text['groupPrivilegesRevokeStemAdminPrivilege'] }</option>
                              <option value="revoke_stemAttrReaders">${textContainer.text['groupPrivilegesRevokeStemAttributeReadPrivilege'] }</option>
                              <option value="revoke_stemAttrUpdaters">${textContainer.text['groupPrivilegesRevokeStemAttributeUpdatePrivilege'] }</option>
                              <option value="revoke_stemViewers">${textContainer.text['groupPrivilegesRevokeStemViewPrivilege'] }</option>
                              <option value="revoke_all">${textContainer.text['groupPrivilegesRevokeAllStemPrivilege'] }</option>
                            </select>
                          </div>
                          <div class="span4">
                            <button type="submit" class="btn" 
                              onclick="ajax('../app/UiV2Stem.assignPrivilegeBatch?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId,stemPrivilegeFormId'}); return false;">${textContainer.text['stemUpdateSelectedPrivilegesButton'] }</button>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <label class="checkbox checkbox-no-padding">
                          <input type="checkbox" name="notImportantXyzName" id="notImportantXyzId" aria-label="${textContainer.text['stemPrivilegesCheckboxAriaLabel'] }"
                           onchange="$('.privilegeCheckbox').prop('checked', $('#notImportantXyzId').prop('checked'));" />
                        </label>
                      </th>
                      <th class="sorted">${textContainer.text['privDropdownName'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">${textContainer.text['priv.colStemAdmin'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">${textContainer.text['priv.colCreate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">${textContainer.text['priv.colStemAttributeRead'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">${textContainer.text['priv.colStemAttributeUpdate'] }</th>
                      <th data-hide="phone" style="white-space: nowrap; text-align: center;">${textContainer.text['priv.colStemView'] }</th>
                      <th style="width:100px;">${textContainer.text['headerChooseAction']}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:set var="i" value="0" />
                    <c:forEach  items="${grouperRequestContainer.stemContainer.privilegeGuiMembershipSubjectContainers}" 
                      var="guiMembershipSubjectContainer" >

                      <tr>
                        <td>
                          <label class="checkbox checkbox-no-padding">
                            <input type="checkbox" name="privilegeSubjectRow_${i}[]" value="${guiMembershipSubjectContainer.guiMember.member.uuid}" class="privilegeCheckbox" />
                          </label>
                        </td>
                        <td class="expand foo-clicker">${guiMembershipSubjectContainer.guiSubject.shortLinkWithIcon}
                        </td>
                        <%-- loop through the fields for stems --%>
                        <c:forEach items="stemAdmins,creators,stemAttrReaders,stemAttrUpdaters,stemViewers" var="fieldName">
                          <td data-hide="phone,medium" class="direct-actions privilege" >
                            <c:set value="${guiMembershipSubjectContainer.guiMembershipContainers[fieldName]}" var="guiMembershipContainer" />
                            <%-- if there is a container, then there is an assignment of some sort... --%>
                            <c:choose>
                              <c:when test="${guiMembershipContainer != null && guiMembershipContainer.membershipContainer.membershipAssignType.immediate}">
                                <i class="fa fa-check fa-direct" tabindex="0" aria-label="${textContainer.text['stemPrivilegesTitleRemoveThisPrivilege'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['stemConfirmChanges']}')) {ajax('../app/UiV2Stem.assignPrivilege?assign=false&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId'});} return false;}"></i>
                                <a title="${textContainer.text['stemPrivilegesTitleRemoveThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['stemConfirmChanges']}')) {ajax('../app/UiV2Stem.assignPrivilege?assign=false&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-times"></i></a>
                              </c:when>
                              <c:otherwise>
                                <c:if test="${guiMembershipContainer != null}"><i class="fa fa-check fa-disabled" tabindex="0" aria-label="${textContainer.text['stemPrivilegesTitleAssignThisPrivilege'] }" onkeydown="if (event.keyCode == 13) {if (confirmChange('${textContainer.textEscapeSingleDouble['stemConfirmChanges']}')) {ajax('../app/UiV2Stem.assignPrivilege?assign=true&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId'});} return false;}"></i></c:if>
                                <a title="${textContainer.text['stemPrivilegesTitleAssignThisPrivilege'] }" class="btn btn-inverse btn-super-mini remove" href="#" 
                                   onclick="if (confirmChange('${textContainer.textEscapeSingleDouble['stemConfirmChanges']}')) {ajax('../app/UiV2Stem.assignPrivilege?assign=true&stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}&fieldName=${fieldName}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}', {formIds: 'stemFilterPrivilegesFormId,stemPagingPrivilegesFormId,stemPagingPrivilegesFormPageNumberId'});} return false;"
                                  ><i class="fa fa-plus"></i></a>
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </c:forEach>
                        <td>
                          <div class="btn-group">
                          	<a data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}" href="#" class="btn btn-mini dropdown-toggle"
                          		aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#stem-privilege-more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#stem-privilege-more-options${i} li').first().focus();return true;});">
                          		${textContainer.text['stemPrivilegeActions'] } 
                          		<span class="caret"></span>
                          	</a>
                            <ul class="dropdown-menu dropdown-menu-right" id="stem-privilege-more-options${i}">
                              <c:if test="${guiMembershipContainer.membershipContainer.membershipAssignType.nonImmediate}">
                                <li><a href="#"  onclick="return guiV2link('operation=UiV2Membership.traceStemPrivileges&stemId=${guiMembershipSubjectContainer.guiStem.stem.id}&memberId=${guiMembershipSubjectContainer.guiMember.member.uuid}'); return false;" class="actions-revoke-membership">${textContainer.text['thisSubjectsPrivilegesActionsMenuTracePrivileges'] }</a></li>
                              </c:if>
                              
                            </ul>
                          </div>
                        </td>
                      </tr>

                      <c:set var="i" value="${i+1}" />
                    </c:forEach>
                  </tbody>
                </table>
              </form>
              <div class="data-table-bottom gradient-background">
                <grouper:paging2 guiPaging="${grouperRequestContainer.stemContainer.privilegeGuiPaging}" formName="stemPagingPrivilegesForm" ajaxFormIds="stemFilterPrivilegesFormId"
                  refreshOperation="../app/UiV2Stem.filterPrivileges?stemId=${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
              </div>
