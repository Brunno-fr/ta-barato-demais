const shippingType = document.querySelector('#shippingType');
const shippingValue = document.querySelector('#shipping-value');
const shippingCost = document.querySelector('#shippingCost');
function updateShippingFields() {
  const paid = shippingType.value === 'PAID';
  shippingValue.hidden = !paid;
  shippingCost.disabled = !paid;
  shippingCost.required = paid;
}
shippingType.addEventListener('change', updateShippingFields);
updateShippingFields();
