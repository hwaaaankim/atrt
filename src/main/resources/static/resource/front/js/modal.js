const contactModal = document.querySelector("#contactModal");
const contactModalBtn = document.querySelector("#contact-modal-button");

contactModalBtn.addEventListener("click", () => {
  contactModal.className = "open";
});

contactModal.addEventListener("click", (e) => {
  if (e.target.id === "yes-btn") {
    ans.innerText = "Yes-Btn Clicked!!!";
  } else if (e.target.id === "no-btn") {
    ans.innerText = "No-Btn Clicked!!!";
  } else {
    return;
  }

  modal.className = "close";
});
