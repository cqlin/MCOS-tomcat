<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<t:block template="/WEB-INF/base-layout.jsp">
<jsp:attribute name="title">
 REM Meal Service - Registration
</jsp:attribute>
<jsp:attribute name="content">
<div class="col-md-6">

<!-- 
		jsp page directive contentType(charset), and pageEncoding only affects response encoding,
	 form accept-charset also doesn't force post content-type charset header.
	 	changing default application/x-www-form-urlencoded to enctype="multipart/form-data" will cause data to 
	 be posted as multipart and need to be read and parsed using multipart.@MultipartConfig()  HttpServletRequest.getParts()
	 Then it is processed same as form field with request.getParameter
-->
<form id="form1" method="post" action="${pageContext.request.contextPath}/public/Registration" accept-charset="UTF-8" autocomplete="off" enctype="application/x-www-form-urlencoded">
	<div class="row">
	<div class="col-md-1"></div>
    <div class="col">
	<h2>Registration</h2> 
    <span id="lblStatus" style="color:${status.color};font-weight:bold"> ${status.status}</span> 
    <span id="lblMessage" style="color:${status.color}">${status.message}</span><br/>
	<span id="lblError" style="color:orange">${status.error}</span><br/>
	<h3>Family information</h3>
	<div id="familyMembers">
		<div class="form-group">
			<label for="email">Email</label>
			<input name="email" type="text" id="email" class="form-control" placeholder="Enter email" autofocus required pattern="[A-Za-z0-9.@_\-]+" title="email address in correct format"/>
		</div>
		<div class="form-group">
			<label for="phone">Phone</label>
			<input name="phone" type="text" id="phone" class="form-control" placeholder="Enter phone number (digits only)" required pattern="[0-9]{10}" title="10 digits phone number"/>
		</div>
		<div class="form-group">
			<label for="lastname[0]">Last Name</label>
			<input name="lastname[0]" type="text" id="lastname[0]" class="form-control" placeholder="Enter lastname" required pattern="\S+" title="last name"/>
		</div>
		<div class="form-group">
			<label for="firstname[0]">First Name</label>
			<input name="firstname[0]" type="text" id="firstname[0]" class="form-control" placeholder="Enter firstname" required pattern="\S+" title="first name"/>
		</div>
	</div>
	<button type="button" id="addMember">Add Family Member</button>&nbsp;&nbsp;&nbsp; <button type="button" id="removeMember">Remove Member</button>
	<br/>
	<hr/>
	<br/>
	<div class="form-group">
		<label for="member">Please leave it blank if you don't have guest card, otherwise scan your code here:</label>
		<input name="member" type="text" id="member" class="form-control" placeholder="" />
		<small id="memberHelp" class="form-text text-muted">guest card will be associated with your information if scanned.</small>
	</div>
	<br/>
	<input type="submit" id="Submit" name="Submit" class="btn btn-outline-primary" />
	</div> 
	<div class="col-md-1"></div>
    </div>
 </form>    

<script>
	$(document).ready(function() {
		/*
		$("#member").focus();
		$("#Submit").click(function() {
			$("#form1").submit();
		})
		*/
		//Prevent enter key submit form
		$(window).keydown(function(event){
		   if(event.keyCode == 13) {
		     event.preventDefault();
		     return false;
		   }
		});

		let memberCount = 1; // Initialize member count
        // Add a new family member input field when the "Add Family Member" button is clicked
        $("#addMember").click(function () {
        	//use javascript template literal to form the html
            const newMember = `<h3>Member #\${memberCount}</h3>
            	<div class="form-group">
            	<label for="lastname[\${memberCount}]">Last Name</label>
            	<input name="lastname[\${memberCount}]" type="text" id="lastname[\${memberCount}]" class="form-control" placeholder="Enter lastname" required pattern="\\S+" title="last name"/>
            	</div>
            	<div class="form-group">
            	<label for="firstname[\${memberCount}]">First Name</label>
            	<input name="firstname[\${memberCount}]" type="text" id="firstname[\${memberCount}]" class="form-control" placeholder="Enter firstname" required pattern="\\S+" title="first name"/>
            	</div>`;
            $("#familyMembers").append(newMember);
            memberCount++; // Increment member count
        });
        $("#removeMember").click(function () {
        	if(memberCount>1){
	            $("#familyMembers").children().last().remove();
	            $("#familyMembers").children().last().remove();
	            $("#familyMembers").children().last().remove();
	            memberCount--; // Decrement member count
        	}
        });
	});
</script>

</div>
</jsp:attribute>
</t:block>

