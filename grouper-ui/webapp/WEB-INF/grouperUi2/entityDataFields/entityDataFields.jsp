<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<div class="bread-header-container">
  <ul class="breadcrumb">
    <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
    <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
      
      <li><a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.viewEntityDataFieldsSummary');">${textContainer.text['miscellaneousEntityDataFieldsBreadcrumb'] }</a><span
      class="divider"><i class='fa fa-angle-right'></i></span></li>
      
    <li class="active">${textContainer.text['miscellaneousDataFieldsBreadcrumb'] }</li>
  </ul>

  <div class="page-header blue-gradient">

    <div class="row-fluid">
      <div class="lead span8 pull-left">
        <h4>${textContainer.text['miscellaneousDataFieldsMainDescription'] }</h4>
      </div>
      <div class="span3 pull-right">
        <%@ include file="dataFieldConfigsMoreActionsButtonContents.jsp"%>
      </div>
    </div>
  </div>
</div>

<div class="row-fluid">

      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['entityDataFieldsHeaderConfigId']}</th>
            <th>${textContainer.text['entityDataFieldsHeaderActions']}</th>
          </tr>
        </thead>
        <tbody>
         <c:set var="i" value="0" />
         <c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.guiDataFieldConfigurations}" var="guiDataFieldConfiguration">
              
            <tr>
              <td style="white-space: nowrap;">
                ${guiDataFieldConfiguration.grouperDataFieldConfiguration.configId}
              </td>

              <td>
                <div class="btn-group">
                  <a data-toggle="dropdown" href="#"
                    aria-label="${textContainer.text['ariaLabelGuiMoreOptions']}"
                    class="btn btn-mini dropdown-toggle"
                    aria-haspopup="true" aria-expanded="false"
                    role="menu"
                    onclick="$('#more-options${i}').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#more-options${i} li').first().focus();return true;});">
                    ${textContainer.text['entityDataFieldsRowActionsButton'] }
                    <span class="caret"></span>
                  </a>
                  <ul class="dropdown-menu dropdown-menu-right"
                    id="more-options${i}">
                    
                    <li><a href="#" onclick="return guiV2link('operation=UiV2EntityDataFields.editDataFieldConfig&dataFieldConfigId=${guiDataFieldConfiguration.grouperDataFieldConfiguration.configId}');">${textContainer.text['dataFieldsEditActionsOption'] }</a></li>

                    <li>&nbsp;</li>                                  
                    <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['dataFieldConfigsConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2EntityDataFields.deleteDataFieldConfig&dataFieldConfigId=${guiDataFieldConfiguration.grouperDataFieldConfiguration.configId}');}">${textContainer.text['dataFieldsDeleteActionsOption'] }</a></li>
                    
                  </ul>
                </div>
               </td>
              </tr>
              
         </c:forEach>
              
        </tbody>
      </table>

</div>
