/**
 * Calculates the total price of a configured menu item.
 * Formula: (basePrice + sum(optionPrices)) * quantity
 */
export const calculateItemPrice = (
  basePrice: number,
  selectedOptions: { incrementalPrice: number }[],
  quantity: number = 1
): number => {
  if (basePrice < 0) return 0;
  if (quantity < 1) quantity = 1;
  const optionsTotal = selectedOptions.reduce((sum, opt) => sum + opt.incrementalPrice, 0);
  return (basePrice + optionsTotal) * quantity;
};
