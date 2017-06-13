import { NmaasGuiPage } from './app.po';

describe('nmaas-portal app', function() {
  let page: NmaasGuiPage;

  beforeEach(() => {
    page = new NmaasGuiPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
