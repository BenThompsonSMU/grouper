<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  
                
         <div class="row-fluid">
         <div class="span9" style="margin-bottom: 20px;">
           <div class="lead">${textContainer.text['stemAttributeAssignmentsTitle'] }</div>
           <span>${textContainer.text['stemAttributeAssignmentsDescription'] }</span>
         </div>
         <div class="span3" id="stemAttributeMoreActionsButtonContentsDivId">
	         <c:if test="${grouperRequestContainer.stemContainer.canUpdateAttributes}">
	           <%@ include file="stemAttributeMoreActionsButtonContents.jsp"%>
	         </c:if>
         </div>
        </div>
        <div class="row-fluid">
         <div id="assign-stem-attribute-block-container" class="well hide">
          <form id="assignAttributeStemForm" class="form-horizontal">
             <input type="hidden" name="stemId" value="${grouperRequestContainer.stemContainer.guiStem.stem.id}" />
             <div class="control-group">
               <label class="control-label">${textContainer.text['stemAssignAttributeAttributeDefNameLabel'] }</label>
               <div class="controls">
                                       
                 <grouper:combobox2 idBase="attributeDefNameCombo" style="width: 30em"
                   filterOperation="UiV2AttributeDefName.attributeDefNameFilter"
                   value="${grouper:escapeHtml(grouperRequestContainer.attributeDefContainer.objectAttributeDefId)}"
                   />
                 <span class="help-block">${textContainer.text['stemAssignAttributeAttributeDefNameDescription'] }</span>
               
               </div>
             </div>
             
             <div class="form-actions"><a href="#" class="btn btn-primary" role="button" onclick="ajax('../app/UiV2StemAttributeAssignment.assignAttributeSubmit', {formIds: 'assignAttributeStemForm'}); return false;">${textContainer.text['groupCreateSaveButton'] }</a> 
             </div>
           </form>
         
         </div>
         </div>
         <!-- This div is filled with the table of existing stem attribute assignments -->
         <div id="viewAttributeAssignments">
           
         </div>
                  
