$(function(){
	if(!searchType || searchType === 'name' || searchType === 'phone' || searchType == 'none' || searchType == 'email'){
		$('#periodSearch').hide();
		$('#subjectSearch').hide();
	}else if(searchType==='period'){
		$('#subjectSearch').hide();
		$('#textSearch').hide();
	}else if(searchType === 'subject'){
		$('#textSearch').hide();
		$('#periodSearch').hide();
	}
	$('#searchType').on('change',function(){
		if($('#searchType option:selected').attr('id')==='searchName' || 
		$('#searchType option:selected').attr('id') === 'searchPhone' ||
		$('#searchType option:selected').attr('id') === 'searchEmail' ||
		$('#searchType option:selected').attr('id') === 'searchBasic'){
			$('#periodSearch').hide();
			$('#subjectSearch').hide();
			$('#textSearch').show();
			$('#textSearch input').val('');
		}else if($('#searchType option:selected').attr('id')==='searchSubject'){
			$('#textSearch').hide();
			$('#periodSearch').hide();
			$('#subjectSearch').show();
		}else if($('#searchType option:selected').attr('id')==='searchPeriod'){
			$('#subjectSearch').hide();
			$('#textSearch').hide();
			$('#periodSearch').show();
			$('#startDate').val(new Date().toISOString().slice(0, 10));
			$('#endDate').val(new Date().toISOString().slice(0, 10));
		}
	});
	
});