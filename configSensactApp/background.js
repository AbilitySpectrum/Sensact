/**
 * Listens for the app launching then creates the window
 *
 * @see http://developer.chrome.com/apps/app.window.html
 */
chrome.app.runtime.onLaunched.addListener(function() {
  chrome.app.window.create('index.html', {
    id: 'main',
    outerBounds: { minWidth: 665, minHeight: 650, maxHeight:650, maxWidth:665}
  });
  

});