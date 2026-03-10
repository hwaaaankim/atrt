(() => {
	const API_BASE = '/admin/api/emailManager';

	let editor;
	let uploadUuid = null;

	let nextCursor = null;
	let hasMore = true;
	let isLoading = false;

	const selectedIds = new Set();

	// Excel Upload DOM
	const $excelFileHidden = document.getElementById('email-manager-excel-file');
	const $excelPickBtn = document.getElementById('email-manager-excel-pick-btn');
	const $excelFilename = document.getElementById('email-manager-excel-filename');
	const $excelUploadBtn = document.getElementById('email-manager-excel-upload-btn');
	const $excelResult = document.getElementById('email-manager-excel-result');

	// Template DOM
	const $templateSaveBtn = document.getElementById('email-manager-template-save-btn');
	const $templateSendBtn = document.getElementById('email-manager-template-send-btn');
	const $templateTestBtn = document.getElementById('email-manager-template-test-btn');
	const $templateStatus = document.getElementById('email-manager-template-status');
	const $subject = document.getElementById('email-manager-subject');

	// List DOM
	const $grid = document.getElementById('email-manager-recipient-grid');
	const $sentinel = document.getElementById('email-manager-scroll-sentinel');
	const $listStatus = document.getElementById('email-manager-list-status');
	const $selectAllBtn = document.getElementById('email-manager-select-all-btn');
	const $clearSelectionBtn = document.getElementById('email-manager-clear-selection-btn');
	const $bulkDeleteBtn = document.getElementById('email-manager-bulk-delete-btn');
	const $deleteAllBtn = document.getElementById('email-manager-delete-all-btn');
	const $selectedCount = document.getElementById('email-manager-selected-count');

	function setBadge(el, text, cls) {
		el.className = 'badge ' + cls;
		el.textContent = text;
	}

	async function fetchJson(url, options = {}) {
		const res = await fetch(url, options);

		if (!res.ok) {
			const text = await res.text();
			throw new Error(`HTTP ${res.status}: ${text}`);
		}

		return res.json();
	}

	function updateSelectionUi() {
		const count = selectedIds.size;

		if ($selectedCount) {
			$selectedCount.textContent = `선택 ${count}건`;
		}

		if ($bulkDeleteBtn) {
			$bulkDeleteBtn.disabled = count === 0;
		}
	}

	function clearSelection() {
		selectedIds.clear();

		$grid.querySelectorAll('.email-manager-recipient-checkbox').forEach(cb => {
			cb.checked = false;
		});

		updateSelectionUi();
	}

	function selectAllVisible() {
		$grid.querySelectorAll('.email-manager-recipient-checkbox').forEach(cb => {
			cb.checked = true;
			selectedIds.add(Number(cb.value));
		});

		updateSelectionUi();
	}

	class EmailManagerUploadAdapter {
		constructor(loader) {
			this.loader = loader;
		}

		async upload() {
			const file = await this.loader.file;
			const form = new FormData();
			form.append('upload', file);
			form.append('uuid', uploadUuid);

			const res = await fetch(`${API_BASE}/editor/image?uuid=${encodeURIComponent(uploadUuid)}`, {
				method: 'POST',
				body: form
			});

			if (!res.ok) {
				throw new Error('이미지 업로드 실패');
			}

			const data = await res.json();
			return { default: data.url };
		}

		abort() {}
	}

	function EmailManagerUploadAdapterPlugin(editor) {
		editor.plugins.get('FileRepository').createUploadAdapter = loader =>
			new EmailManagerUploadAdapter(loader);
	}

	async function initUuid() {
		const data = await fetchJson(`${API_BASE}/uuid`);
		uploadUuid = data.uuid;
	}

	async function initEditor() {
		const tpl = await fetchJson(`${API_BASE}/template`);
		$subject.value = tpl.subject || '';

		editor = await ClassicEditor.create(
			document.querySelector('#email-manager-editor'),
			{ extraPlugins: [EmailManagerUploadAdapterPlugin] }
		);

		editor.setData(tpl.html || '');
	}

	async function saveTemplate(showMessage = true) {
		const html = editor.getData();
		const subject = ($subject.value || '').trim();

		if (showMessage) {
			$templateStatus.textContent = '저장 중...';
		}

		await fetchJson(`${API_BASE}/template`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify({ subject, html })
		});

		if (showMessage) {
			$templateStatus.textContent = '저장 완료';
			setTimeout(() => ($templateStatus.textContent = ''), 1500);
		}
	}

	async function sendNow() {
		if (!confirm('현재 작성 중인 내용을 먼저 저장한 뒤 전체 이메일에 발송을 시작합니다.\n\n진행하시겠습니까?')) {
			return;
		}

		try {
			await saveTemplate(false);

			$templateStatus.textContent = '발송 시작 요청 중...';

			const data = await fetchJson(`${API_BASE}/send`, {
				method: 'POST'
			});

			$templateStatus.textContent = `발송 시작됨 (jobId: ${data.jobId})`;
			setTimeout(() => ($templateStatus.textContent = ''), 4000);

		} catch (e) {
			console.error(e);
			alert('발송 시작에 실패했습니다.\n\n' + e.message);
			$templateStatus.textContent = '발송 시작 실패';
			setTimeout(() => ($templateStatus.textContent = ''), 3000);
		}
	}

	async function sendTest() {
		if (!confirm('contact@atrt.co.kr 로 메일이 발송됩니다.\n\n테스트 발송하시겠습니까?')) {
			return;
		}

		try {
			const subject = ($subject.value || '').trim();
			const html = editor.getData();

			$templateStatus.textContent = '테스트 발송 중...';

			await fetchJson(`${API_BASE}/send/test`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ subject, html })
			});

			alert('contact@atrt.co.kr 로 메일이 발송되었습니다.');
			$templateStatus.textContent = '테스트 발송 완료';
			setTimeout(() => ($templateStatus.textContent = ''), 3000);

		} catch (e) {
			console.error(e);
			alert('테스트 발송에 실패했습니다.\n\n' + e.message);
			$templateStatus.textContent = '테스트 발송 실패';
			setTimeout(() => ($templateStatus.textContent = ''), 3000);
		}
	}

	function createRecipientCard(item) {
		const col = document.createElement('div');
		col.className = 'col-12 col-sm-6 col-md-4 col-lg-3';
		col.dataset.recipientId = item.id;

		const card = document.createElement('div');
		card.className = 'email-manager-recipient-card';

		const top = document.createElement('div');
		top.className = 'email-manager-recipient-top';

		const left = document.createElement('div');
		left.className = 'email-manager-recipient-left';

		const checkbox = document.createElement('input');
		checkbox.type = 'checkbox';
		checkbox.className = 'form-check-input email-manager-recipient-checkbox';
		checkbox.value = item.id;

		if (selectedIds.has(Number(item.id))) {
			checkbox.checked = true;
		}

		checkbox.addEventListener('change', (e) => {
			const id = Number(e.target.value);

			if (e.target.checked) {
				selectedIds.add(id);
			} else {
				selectedIds.delete(id);
			}

			updateSelectionUi();
		});

		const email = document.createElement('div');
		email.className = 'email-manager-recipient-email';
		email.textContent = item.email;

		left.appendChild(checkbox);
		left.appendChild(email);

		const delBtn = document.createElement('button');
		delBtn.type = 'button';
		delBtn.className = 'email-manager-recipient-delete-btn';

		const delIcon = document.createElement('span');
		delIcon.className = 'email-manager-recipient-delete-icon';
		delIcon.textContent = '×';

		delBtn.appendChild(delIcon);
		delBtn.addEventListener('click', () => deleteRecipient(item.id, item.email));

		top.appendChild(left);
		top.appendChild(delBtn);

		const meta = document.createElement('div');
		meta.className = 'email-manager-recipient-meta';
		meta.textContent = `ID: ${item.id}`;

		card.appendChild(top);
		card.appendChild(meta);
		col.appendChild(card);

		return col;
	}

	async function deleteRecipient(id, email) {
		if (!confirm(`이 이메일을 삭제하시겠습니까?\n\n${email}`)) {
			return;
		}

		try {
			$listStatus.textContent = '삭제 중...';

			await fetchJson(`${API_BASE}/recipients/${id}`, {
				method: 'DELETE'
			});

			selectedIds.delete(Number(id));
			updateSelectionUi();

			await reloadRecipients();

			$listStatus.textContent = hasMore ? '스크롤 시 추가 로딩' : '마지막입니다';
		} catch (e) {
			console.error(e);
			alert('삭제에 실패했습니다.\n\n' + e.message);
			$listStatus.textContent = '삭제 실패';
		}
	}

	async function bulkDeleteSelected() {
		if (selectedIds.size === 0) {
			return;
		}

		if (!confirm(`선택한 ${selectedIds.size}건의 이메일을 삭제하시겠습니까?`)) {
			return;
		}

		try {
			$listStatus.textContent = '선택 삭제 중...';

			const ids = Array.from(selectedIds);

			const data = await fetchJson(`${API_BASE}/recipients/bulk-delete`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ ids })
			});

			clearSelection();
			await reloadRecipients();

			alert(`${data.deleted}건 삭제되었습니다.`);
			$listStatus.textContent = hasMore ? '스크롤 시 추가 로딩' : '마지막입니다';
		} catch (e) {
			console.error(e);
			alert('선택 삭제에 실패했습니다.\n\n' + e.message);
			$listStatus.textContent = '선택 삭제 실패';
		}
	}

	async function deleteAllRecipients() {
		if (!confirm('모든 이메일을 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.')) {
			return;
		}

		try {
			$listStatus.textContent = '전체 삭제 중...';

			const data = await fetchJson(`${API_BASE}/recipients/all`, {
				method: 'DELETE'
			});

			clearSelection();
			await reloadRecipients();

			alert(`전체 삭제 완료 (${data.deleted}건)`);
			$listStatus.textContent = '전체 삭제 완료';
		} catch (e) {
			console.error(e);
			alert('전체 삭제에 실패했습니다.\n\n' + e.message);
			$listStatus.textContent = '전체 삭제 실패';
		}
	}

	async function loadRecipientsNext() {
		if (!hasMore || isLoading) {
			return;
		}

		isLoading = true;
		$listStatus.textContent = '불러오는 중...';

		try {
			const url = new URL(window.location.origin + `${API_BASE}/recipients`);
			url.searchParams.set('size', '60');

			if (nextCursor != null) {
				url.searchParams.set('cursor', nextCursor);
			}

			const data = await fetchJson(url.toString().replace(window.location.origin, ''));

			const items = data.items || [];
			items.forEach(it => $grid.appendChild(createRecipientCard(it)));

			nextCursor = data.nextCursor;
			hasMore = !!data.hasMore;

			$listStatus.textContent = hasMore ? '스크롤 시 추가 로딩' : '마지막입니다';
			$sentinel.textContent = hasMore
				? '스크롤하면 더 불러옵니다.'
				: '더 이상 데이터가 없습니다.';

			updateSelectionUi();

		} finally {
			isLoading = false;
		}
	}

	async function reloadRecipients() {
		$grid.innerHTML = '';
		nextCursor = null;
		hasMore = true;
		$sentinel.textContent = '스크롤하면 더 불러옵니다.';
		await loadRecipientsNext();
	}

	function bindExcelPicker() {
		$excelPickBtn.addEventListener('click', () => $excelFileHidden.click());

		$excelFileHidden.addEventListener('change', () => {
			const f = $excelFileHidden.files?.[0];
			$excelFilename.value = f ? f.name : '';
			setBadge($excelResult, '대기', 'bg-secondary');
		});
	}

	async function uploadExcel() {
		const file = $excelFileHidden.files?.[0];

		if (!file) {
			setBadge($excelResult, '파일을 선택해주세요', 'bg-danger');
			return;
		}

		try {
			setBadge($excelResult, '업로드 중...', 'bg-warning');

			const form = new FormData();
			form.append('file', file);

			const data = await fetchJson(`${API_BASE}/recipients/excel`, {
				method: 'POST',
				body: form
			});

			setBadge($excelResult, `저장 완료 (+${data.inserted}개)`, 'bg-success');

			clearSelection();
			await reloadRecipients();

			$excelFileHidden.value = '';
			$excelFilename.value = '';

		} catch (e) {
			console.error(e);
			setBadge($excelResult, '업로드 실패', 'bg-danger');
			alert('엑셀 업로드에 실패했습니다.\n\n' + e.message);
		}
	}

	function initInfiniteScroll() {
		const io = new IntersectionObserver(
			(entries) => {
				if (entries.some(e => e.isIntersecting)) {
					loadRecipientsNext();
				}
			},
			{ rootMargin: '200px' }
		);

		io.observe($sentinel);
	}

	async function boot() {
		setBadge($excelResult, '대기', 'bg-secondary');

		bindExcelPicker();

		await initUuid();
		await initEditor();

		$templateSaveBtn.addEventListener('click', () => saveTemplate(true));
		$templateSendBtn.addEventListener('click', sendNow);
		$templateTestBtn.addEventListener('click', sendTest);

		$excelUploadBtn.addEventListener('click', uploadExcel);

		$selectAllBtn.addEventListener('click', selectAllVisible);
		$clearSelectionBtn.addEventListener('click', clearSelection);
		$bulkDeleteBtn.addEventListener('click', bulkDeleteSelected);
		$deleteAllBtn.addEventListener('click', deleteAllRecipients);

		initInfiniteScroll();
		await loadRecipientsNext();
		updateSelectionUi();
	}

	boot();
})();