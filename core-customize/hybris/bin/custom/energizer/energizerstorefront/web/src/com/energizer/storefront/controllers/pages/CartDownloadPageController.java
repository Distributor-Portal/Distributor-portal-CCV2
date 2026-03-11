/**
 *
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.order.CartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.impex.ExportService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCartFacade;
import com.energizer.facades.flow.impl.DefaultEnergizerB2BCheckoutFlowFacade;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;


@Controller
@Scope("tenant")


public class CartDownloadPageController extends AbstractSearchPageController
{

	protected static final Logger LOG = Logger.getLogger(CartDownloadPageController.class);

	@Resource(name = "accountBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

	private static final String CART_DOWNLOAD_PAGE = "cartdownload";

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource
	private ExportService exportService;

	@Autowired
	private ConfigurationService configurationService;

	@Resource
	UserService userService;

	@Resource(name = "b2bUnitService")
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

	@Resource(name = "enumerationService")
	private EnumerationService enumerationService;

	@Resource
	private CartService cartService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Deprecated
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "storeSessionFacade")
	private StoreSessionFacade storeSessionFacade;

	private static final int BUFFER_SIZE = 4096;
	private static final String REDIRECT_PREFIX = "redirect:";

	@Resource(name = "energizerB2BCheckoutFlowFacade")
	private DefaultEnergizerB2BCheckoutFlowFacade energizerB2BCheckoutFlowFacade;

	@Resource
	DefaultEnergizerB2BCartFacade energizerB2BCartFacade;

	@Resource(name = "productService")
	private ProductService productService;

	@RequestMapping(value = "/downloadCart", method = RequestMethod.GET)
	//, method = RequestMethod.POST)
	@RequireHardLogIn
	public String downloadCart(final Model model, final RedirectAttributes redirectAttributes, final HttpServletRequest request,
			final HttpServletResponse response) throws CMSItemNotFoundException
	{
		LOG.info("cart downnload page");
		try
		{
			LOG.info("cart Download");

			final String userId = userService.getCurrentUser().getUid();
			final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
			final CartData cartData = cartFacade.getSessionCart();


			if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
			{
				LOG.info("B2B Unit ID : " + b2bUnit.getUid());
				LOG.info("Site ID : " + this.getSiteUid());

				final List<OrderEntryData> cartList = cartData.getEntries();
				final boolean isCartDownload = true;
				final String currencyCode = populateCurrency();
				final String fileName = writingToExcel(request, cartList, isCartDownload, currencyCode, this.getSiteUid());
				saveFile(request, response, isCartDownload, fileName);
			}

		}

		catch (final Exception e)
		{
			LOG.info("Exception occurred while downloading cart :::: " + e.getMessage());
			e.printStackTrace();

		}
		LOG.info("return to cart");
		return REDIRECT_PREFIX + "/cart";


	}

	/**
	 * @param request
	 * @param response
	 * @param isCartDownload
	 */
	public void saveFile(final HttpServletRequest request, final HttpServletResponse response, final boolean iscartDownload,
			final String fileName) throws FileNotFoundException, IOException, Exception
	{


		final String exportDir = configurationService.getConfiguration().getString("cartdownload.downloadPath");
		final ServletContext context = request.getServletContext();
		//String fileName = null;
		//final CartData cartData = cartFacade.getSessionCart();
		//final String cartId = cartData.getCode();

		/*-if (iscartDownload)
		{
			fileName = "cartDownload(" + cartId + ").xls";
		}*/


		final String fullPath = System.getProperty("user.home") + "\\" + exportDir + "\\" + fileName + ".xls";
		final File downloadFile = new File(fullPath);
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(downloadFile);
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
			throw e;
		}

		String mimeType = context.getMimeType(fullPath);
		if (mimeType == null)
		{
			mimeType = "application/octet-stream";
		}
		LOG.info("MIME type: " + mimeType);

		// set content attributes for the response
		response.setContentType(mimeType);
		response.setContentLength((int) downloadFile.length());


		// set headers for the response
		final String headerKey = "Content-Disposition";
		final String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);

		OutputStream outStream = null;
		try
		{
			outStream = response.getOutputStream();
		}
		catch (final IOException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

		final byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;

		// write bytes read from the input stream into the output stream
		try
		{
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				outStream.write(buffer, 0, bytesRead);

				final OutputStreamWriter outputStreamWriter;

			}
		}
		catch (final IOException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

		try
		{

			inputStream.close();
			outStream.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
			throw e;

		}




	}

	/**
	 * @param cartList
	 * @param iscartDownload
	 * @param currencyCode
	 * @param siteUid
	 */
	private String writingToExcel(final HttpServletRequest request, final List<OrderEntryData> cartList,
			final boolean isCartDownload, final String currencyCode, final String site) throws Exception
	{

		final String exportDownloadDir = configurationService.getConfiguration().getString("cartdownload.downloadPath");

		final String PRICE_COLUMN = "Price";
		String headerColumns = null;
		final boolean isSalesRepUser = (boolean) sessionService.getAttribute("isSalesRepUserLoggedIn");
		final boolean gotPriceFromSAP = (boolean) request.getSession().getAttribute("gotPriceFromSAP");

		LOG.info("gotPriceFromSAP :: " + gotPriceFromSAP);

		// File naming convention =>  <B2BUnitID>_<Date Timestamp in yyyy-MM-dd HHmmss format>
		final EnergizerB2BCustomerModel b2bcustomer = (EnergizerB2BCustomerModel) userService.getCurrentUser();
		final EnergizerB2BUnitModel unit = (EnergizerB2BUnitModel) b2bUnitService.getParent(b2bcustomer);
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
		final Date date = new Date();
		final String strDate = dateFormat.format(date);
		final String sheetName = "CART_" + unit.getUid() + "_" + strDate;
		final String fileName = "CART_" + unit.getName().toUpperCase() + "_" + strDate;

		if (isCartDownload)
		{
			if (site.equalsIgnoreCase(configurationService.getConfiguration().getString("site.personalCare")))
			{
				if (!isSalesRepUser)
				{
					headerColumns = configurationService.getConfiguration().getString("excel.download.header.columns.personalCare");
				}
				else
				{
					headerColumns = configurationService.getConfiguration()
							.getString("excel.cart.download.header.columns.personalCare.wesell");
				}
			}

		}

		if (headerColumns != null && !headerColumns.isEmpty())
		{
			final String[] columnArray = headerColumns.split(",");

			try
			{
				//final CartData cartData = cartFacade.getSessionCart();
				//final String cartId = cartData.getCode();


				final HSSFWorkbook hwb = new HSSFWorkbook();

				final HSSFSheet sheet = hwb.createSheet(sheetName);

				int rownum = 0;
				final int columnnum = 0;

				HSSFRow row = sheet.createRow(rownum);
				HSSFCell cell = row.createCell(columnnum);
				final StringBuilder sb = new StringBuilder();


				if (rownum == 0)
				{
					for (int k = 0; k < columnArray.length; k++)
					{
						cell = row.createCell(k);
						final String rowheader = columnArray[k];
						if (rowheader.equalsIgnoreCase(PRICE_COLUMN))
						{
							cell.setCellValue(sb.append(rowheader).append("(").append(currencyCode).append(")").toString());
						}
						else
						{
							cell.setCellValue(rowheader);
						}
					}

				}



				for (final OrderEntryData entry : cartList)
				{
					row = sheet.createRow(++rownum);
					final ProductModel product = productService.getProductForCode(entry.getProduct().getCode());

					//final String objData = entry.toString();
					//final String[] arrdata = objData.split(",");

					//for (int i = 0; i < 11; i++)
					//{
					String marerialId = StringUtils.EMPTY;
					cell = row.createCell(0);
					if (entry.getProduct().getCode() != null && !entry.getProduct().getCode().isEmpty())
					{
						marerialId = entry.getProduct().getCode().trim();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					marerialId = marerialId.replaceAll("\"", "");
					marerialId = marerialId.replaceAll("=", "");
					cell.setCellValue(marerialId);

					String custMaterialID = StringUtils.EMPTY;
					cell = row.createCell(1);
					if (entry.getProduct().getCustomerMaterialId() != null && !entry.getProduct().getCustomerMaterialId().isEmpty())
					{
						custMaterialID = entry.getProduct().getCustomerMaterialId().trim();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(custMaterialID);

					String uom = StringUtils.EMPTY;
					cell = row.createCell(2);
					if (entry.getProduct().getUom() != null && !entry.getProduct().getUom().isEmpty())
					{
						uom = entry.getProduct().getUom().trim();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(uom);

					String quantity = StringUtils.EMPTY;
					cell = row.createCell(3);
					if (entry.getQuantity() != null)
					{
						quantity = entry.getQuantity().toString().trim();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(quantity);

					String description = StringUtils.EMPTY;
					cell = row.createCell(4);
					if (entry.getProduct().getName() != null && !entry.getProduct().getName().isEmpty())
					{
						description = entry.getProduct().getName().trim();

					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(description);

					String unitsIneach = StringUtils.EMPTY;
					cell = row.createCell(5);
					if (entry.getProduct().getBaseUOM() != null)
					{
						unitsIneach = entry.getProduct().getBaseUOM().toString();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(unitsIneach);


					String stock = StringUtils.EMPTY;
					cell = row.createCell(6);
					//if gotPriceFromSAP = true, then load stock from DB else stock column should be empty in the excel sheet
					if (entry.getInventoryAvailable() != null && !entry.getInventoryAvailable().isEmpty() && gotPriceFromSAP)
					{
						stock = entry.getInventoryAvailable().trim();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(stock);

					String currency = StringUtils.EMPTY;
					cell = row.createCell(7);
					//if gotPriceFromSAP = true, then load currency from DB else currency column should be empty in the excel sheet
					if (entry.getBasePrice().getCurrencyIso() != null && !entry.getBasePrice().getCurrencyIso().isEmpty()
							&& gotPriceFromSAP)
					{
						currency = entry.getBasePrice().getCurrencyIso().trim();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(currency);

					String priceUOM = StringUtils.EMPTY;
					cell = row.createCell(8);
					//if gotPriceFromSAP = true, then load prices from DB else price column should be empty in the excel sheet
					if (entry.getBasePrice() != null && entry.getBasePrice().getValue().intValue() > 0 && gotPriceFromSAP)
					{
						priceUOM = entry.getBasePrice().getValue().toString();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(priceUOM);


					String discount = StringUtils.EMPTY;
					cell = row.createCell(9);
					//if gotPriceFromSAP = true, then load discount% from DB else discount column should be empty in the excel sheet
					if (entry.getDiscountPercent() != null && !entry.getDiscountPercent().isEmpty() && gotPriceFromSAP)
					{
						LOG.info("Not null percent");
						discount = entry.getDiscountPercent().toString();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(discount);


					String total = StringUtils.EMPTY;
					cell = row.createCell(10);
					//if gotPriceFromSAP = true, then load total prices from DB else total price column should be empty in the excel sheet
					if (entry.getTotalPrice() != null && entry.getTotalPrice().getValue().intValue() > 0 && gotPriceFromSAP)
					{
						total = entry.getTotalPrice().getValue().toString();
					}
					//cell.setCellType(Cell.CELL_TYPE_STRING);
					cell.setCellType(CellType.STRING);
					cell.setCellValue(total);
				}




				final File dir = new File(System.getProperty("user.home") + "\\" + exportDownloadDir);

				//final Path dirPath = Paths.get(dir.getAbsolutePath());
				FileOutputStream fileOut = null;

				if (isCartDownload)
				{
					//fileOut = new FileOutputStream(dir + "\\" + "cartDownload(" + cartId + ").xls");
					fileOut = new FileOutputStream(dir + "\\" + fileName + ".xls");
					LOG.info("Cart Download file name : " + fileName + ".xls");
				}

				hwb.write(fileOut);
				fileOut.close();

			}
			catch (final Exception ex)
			{
				LOG.info("Exception occurred while writing cart Data to Excel::: " + ex);
				ex.printStackTrace();
				throw ex;
			}
		}


		return fileName;

	}

	protected String populateCurrency()
	{
		return storeSessionFacade.getCurrentCurrency().getIsocode();
	}


}
